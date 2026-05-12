package com.example.analyzer.controller;

import com.example.analyzer.model.AnalysisResult;
import com.example.analyzer.service.AnalyzerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnalyzerController {

    private final AnalyzerService analyzerService;

    public AnalyzerController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
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

    @GetMapping("/health")
    public String health() {
        return "redirect:/";
    }
}
