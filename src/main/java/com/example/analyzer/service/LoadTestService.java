package com.example.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadTestService {

    private static final int TOTAL_SECONDS = 30;
    private static final int MAX_CONCURRENCY = 10;
    private static final int RAMP_STEP_SECONDS = 3;
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; WebsiteAnalyzer-LoadTest/1.0)";

    private final ExecutorService runners = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "loadtest-runner");
        t.setDaemon(true);
        return t;
    });

    public void run(String targetUrl, SseEmitter emitter) {
        runners.submit(() -> {
            try {
                runInternal(targetUrl, emitter);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error")
                            .data(Map.of("message", String.valueOf(e.getMessage()))));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });
    }

    private void runInternal(String targetUrl, SseEmitter emitter) throws Exception {
        URI uri = URI.create(targetUrl);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger activeWorkers = new AtomicInteger(0);
        ConcurrentLinkedQueue<Sample> samples = new ConcurrentLinkedQueue<>();
        ExecutorService workerPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "loadtest-worker");
            t.setDaemon(true);
            return t;
        });

        long startNanos = System.nanoTime();
        long endNanos = startNanos + TimeUnit.SECONDS.toNanos(TOTAL_SECONDS);

        Runnable workerTask = () -> {
            while (running.get() && System.nanoTime() < endNanos) {
                long t0 = System.nanoTime();
                boolean ok = false;
                try {
                    HttpResponse<Void> resp = client.send(request,
                            HttpResponse.BodyHandlers.discarding());
                    int status = resp.statusCode();
                    ok = status >= 200 && status < 400;
                } catch (Exception e) {
                    ok = false;
                }
                long ms = (System.nanoTime() - t0) / 1_000_000L;
                samples.add(new Sample(System.nanoTime(), ms, ok));
            }
        };

        emitter.send(SseEmitter.event().name("start")
                .data(Map.of(
                        "targetUrl", targetUrl,
                        "totalSeconds", TOTAL_SECONDS,
                        "maxConcurrency", MAX_CONCURRENCY)));

        workerPool.submit(workerTask);
        activeWorkers.set(1);

        for (int sec = 0; sec < TOTAL_SECONDS; sec++) {
            long bucketEnd = startNanos + TimeUnit.SECONDS.toNanos(sec + 1L);
            int targetConcurrency = Math.min(1 + (sec / RAMP_STEP_SECONDS), MAX_CONCURRENCY);
            while (activeWorkers.get() < targetConcurrency) {
                workerPool.submit(workerTask);
                activeWorkers.incrementAndGet();
            }

            while (System.nanoTime() < bucketEnd) {
                Thread.sleep(25);
            }

            List<Sample> bucket = new ArrayList<>();
            Iterator<Sample> it = samples.iterator();
            while (it.hasNext()) {
                Sample s = it.next();
                if (s.timeNanos <= bucketEnd) {
                    bucket.add(s);
                    it.remove();
                }
            }

            long count = bucket.size();
            long errors = bucket.stream().filter(s -> !s.ok).count();
            double avg = bucket.stream().mapToLong(s -> s.ms).average().orElse(0.0);
            long min = bucket.stream().mapToLong(s -> s.ms).min().orElse(0);
            long max = bucket.stream().mapToLong(s -> s.ms).max().orElse(0);

            Map<String, Object> payload = new HashMap<>();
            payload.put("second", sec + 1);
            payload.put("concurrency", targetConcurrency);
            payload.put("count", count);
            payload.put("errors", errors);
            payload.put("avgMs", Math.round(avg));
            payload.put("minMs", min);
            payload.put("maxMs", max);
            emitter.send(SseEmitter.event().name("tick").data(payload));
        }

        running.set(false);
        workerPool.shutdownNow();
        emitter.send(SseEmitter.event().name("done").data(Map.of()));
        emitter.complete();
    }

    private record Sample(long timeNanos, long ms, boolean ok) {}
}
