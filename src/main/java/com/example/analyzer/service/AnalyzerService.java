package com.example.analyzer.service;

import com.example.analyzer.model.AnalysisResult;
import com.example.analyzer.model.SpecialFile;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalyzerService {

    private static final int TIMEOUT_MS = 15000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; WebsiteAnalyzer/1.0; +https://github.com/)";

    private static final List<SpecialFile> SPECIAL_FILE_DEFS = List.of(
            new SpecialFile("robots.txt", "/robots.txt",
                    "https://en.wikipedia.org/wiki/Robots.txt",
                    "Tells web crawlers which paths they may or may not request."),
            new SpecialFile("sitemap.xml", "/sitemap.xml",
                    "https://en.wikipedia.org/wiki/Sitemaps",
                    "Lists the pages of a site to help search engines crawl it."),
            new SpecialFile("humans.txt", "/humans.txt",
                    "https://humanstxt.org/",
                    "Credits the people behind the site (team, tech, thanks)."),
            new SpecialFile("security.txt", "/.well-known/security.txt",
                    "https://securitytxt.org/",
                    "Standard contact info for reporting security vulnerabilities (RFC 9116)."),
            new SpecialFile("ads.txt", "/ads.txt",
                    "https://iabtechlab.com/ads-txt/",
                    "Declares which companies are authorized to sell the site's ad inventory."),
            new SpecialFile("favicon.ico", "/favicon.ico",
                    "https://en.wikipedia.org/wiki/Favicon",
                    "The small icon browsers display for the site in tabs and bookmarks."),
            new SpecialFile("manifest.json", "/manifest.json",
                    "https://developer.mozilla.org/en-US/docs/Web/Manifest",
                    "Web app manifest: name, icons, and settings for installable PWAs.")
    );

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "for", "are", "but", "not", "you", "all", "can", "her",
            "was", "one", "our", "out", "day", "get", "has", "him", "his", "how",
            "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did",
            "its", "let", "put", "say", "she", "too", "use", "with", "this", "that",
            "from", "have", "they", "will", "your", "what", "when", "make", "like",
            "time", "just", "know", "take", "into", "year", "good", "some", "them",
            "than", "then", "look", "only", "come", "over", "also", "back", "after",
            "work", "well", "even", "want", "because", "any", "these", "give", "most",
            "should", "would", "could", "their", "there", "which", "about", "more",
            "been", "such", "very", "much", "many", "where", "while", "those", "here"
    ));

    public AnalysisResult analyze(String inputUrl) throws Exception {
        String url = normalizeUrl(inputUrl);

        long start = System.currentTimeMillis();
        Connection.Response response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .execute();
        long elapsed = System.currentTimeMillis() - start;

        AnalysisResult result = new AnalysisResult();
        result.setUrl(url);
        result.setFinalUrl(response.url().toString());
        result.setStatusCode(response.statusCode());
        result.setResponseTimeMs(elapsed);
        result.setContentType(response.contentType());
        result.setServer(response.header("Server"));
        result.setCharset(response.charset());
        result.setUsesHttps(response.url().getProtocol().equalsIgnoreCase("https"));

        String body = response.body();
        result.setContentLengthBytes(body == null ? 0 : body.getBytes().length);

        probeSpecialFiles(response.url().toString(), result);

        String contentType = response.contentType();
        if (contentType == null || !contentType.toLowerCase().contains("html")) {
            result.setHeadingCounts(emptyHeadingCounts());
            return result;
        }

        Document doc = Jsoup.parse(body, response.url().toString());
        populateFromDocument(doc, result);
        return result;
    }

    private void probeSpecialFiles(String pageUrl, AnalysisResult result) {
        URI base;
        try {
            base = URI.create(pageUrl);
        } catch (Exception e) {
            return;
        }

        List<SpecialFile> probed = new ArrayList<>();
        for (SpecialFile def : SPECIAL_FILE_DEFS) {
            SpecialFile sf = new SpecialFile(def.getName(), def.getPath(),
                    def.getInfoUrl(), def.getDescription());
            try {
                URI target = new URI(base.getScheme(), null, base.getHost(), base.getPort(),
                        def.getPath(), null, null);
                String url = target.toString();
                sf.setUrl(url);

                Connection.Response resp = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .followRedirects(true)
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .method(Connection.Method.GET)
                        .execute();
                sf.setStatus(resp.statusCode());

                if ("robots.txt".equals(def.getName())) {
                    result.setRobotsTxtUrl(url);
                    result.setRobotsTxtStatus(resp.statusCode());
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        String content = resp.body();
                        if (content == null) content = "";
                        if (content.length() > 20000) {
                            content = content.substring(0, 20000) + "\n... [truncated]";
                        }
                        result.setRobotsTxtContent(content);
                    }
                }
            } catch (Exception e) {
                sf.setError(e.getMessage());
                if ("robots.txt".equals(def.getName())) {
                    result.setRobotsTxtError(e.getMessage());
                }
            }
            probed.add(sf);
        }
        result.setSpecialFiles(probed);
    }

    private void populateFromDocument(Document doc, AnalysisResult result) {
        result.setTitle(doc.title());

        Element descMeta = doc.selectFirst("meta[name=description]");
        if (descMeta != null) {
            result.setMetaDescription(descMeta.attr("content"));
        }

        Element kwMeta = doc.selectFirst("meta[name=keywords]");
        if (kwMeta != null) {
            result.setMetaKeywords(kwMeta.attr("content"));
        }

        Element html = doc.selectFirst("html");
        if (html != null) {
            String lang = html.attr("lang");
            if (!lang.isEmpty()) {
                result.setLanguage(lang);
            }
        }

        Map<String, Integer> headings = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            headings.put("h" + i, doc.select("h" + i).size());
        }
        result.setHeadingCounts(headings);

        Elements links = doc.select("a[href]");
        int total = 0;
        int internal = 0;
        int external = 0;
        String host = hostOf(result.getFinalUrl());
        for (Element a : links) {
            String href = a.attr("abs:href");
            if (href.isEmpty()) {
                continue;
            }
            total++;
            String linkHost = hostOf(href);
            if (linkHost == null || linkHost.isEmpty() || linkHost.equalsIgnoreCase(host)) {
                internal++;
            } else {
                external++;
            }
        }
        result.setTotalLinks(total);
        result.setInternalLinks(internal);
        result.setExternalLinks(external);

        Elements images = doc.select("img");
        result.setImages(images.size());
        int missingAlt = 0;
        for (Element img : images) {
            if (img.attr("alt").trim().isEmpty()) {
                missingAlt++;
            }
        }
        result.setImagesMissingAlt(missingAlt);

        result.setScripts(doc.select("script").size());
        result.setStylesheets(doc.select("link[rel=stylesheet]").size());
        result.setForms(doc.select("form").size());

        result.setHasViewportMeta(doc.selectFirst("meta[name=viewport]") != null);
        result.setHasCanonical(doc.selectFirst("link[rel=canonical]") != null);
        result.setHasFavicon(doc.selectFirst("link[rel~=(?i)icon]") != null);

        String text = doc.body() == null ? "" : doc.body().text();
        String[] words = text.toLowerCase().split("[^a-z0-9']+");
        int wordCount = 0;
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            if (w.length() < 4 || STOP_WORDS.contains(w)) {
                continue;
            }
            wordCount++;
            freq.merge(w, 1, Integer::sum);
        }
        result.setWordCount(wordCount);

        List<String> top = freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.toList());
        result.setTopKeywords(top);
    }

    private Map<String, Integer> emptyHeadingCounts() {
        Map<String, Integer> headings = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            headings.put("h" + i, 0);
        }
        return headings;
    }

    private String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeUrl(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }
        if (!trimmed.matches("(?i)^https?://.*")) {
            trimmed = "https://" + trimmed;
        }
        return trimmed;
    }
}
