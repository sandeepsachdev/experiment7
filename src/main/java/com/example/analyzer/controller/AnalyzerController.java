package com.example.analyzer.controller;

import com.example.analyzer.model.AnalysisResult;
import com.example.analyzer.service.AnalyzerService;
import com.example.analyzer.service.LoadTestService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class AnalyzerController {

    private final AnalyzerService analyzerService;
    private final LoadTestService loadTestService;

    public AnalyzerController(AnalyzerService analyzerService,
                              LoadTestService loadTestService) {
        this.analyzerService = analyzerService;
        this.loadTestService = loadTestService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("url") String url, Model model) {
        try {
            AnalysisResult result = analyzerService.analyze(url);
            model.addAttribute("result", result);
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Could not analyze the URL: " + e.getMessage());
            model.addAttribute("url", url);
            return "index";
        }
    }

    @GetMapping(value = "/loadtest/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter loadTest(@RequestParam("url") String url) {
        SseEmitter emitter = new SseEmitter(90_000L);
        loadTestService.run(url, emitter);
        return emitter;
    }

    @GetMapping("/health")
    public String health() {
        return "redirect:/";
    }
}
