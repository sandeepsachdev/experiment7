package com.example.analyzer.model;

import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private String url;
    private String finalUrl;
    private int statusCode;
    private long responseTimeMs;
    private long contentLengthBytes;
    private String contentType;
    private String server;
    private String title;
    private String metaDescription;
    private String metaKeywords;
    private String language;
    private String charset;
    private Map<String, Integer> headingCounts;
    private int totalLinks;
    private int internalLinks;
    private int externalLinks;
    private int images;
    private int imagesMissingAlt;
    private int scripts;
    private int stylesheets;
    private int forms;
    private int wordCount;
    private List<String> topKeywords;
    private boolean usesHttps;
    private boolean hasViewportMeta;
    private boolean hasCanonical;
    private boolean hasFavicon;
    private String robotsTxtUrl;
    private Integer robotsTxtStatus;
    private String robotsTxtContent;
    private String robotsTxtError;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFinalUrl() { return finalUrl; }
    public void setFinalUrl(String finalUrl) { this.finalUrl = finalUrl; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public long getContentLengthBytes() { return contentLengthBytes; }
    public void setContentLengthBytes(long contentLengthBytes) { this.contentLengthBytes = contentLengthBytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }

    public String getMetaKeywords() { return metaKeywords; }
    public void setMetaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }

    public Map<String, Integer> getHeadingCounts() { return headingCounts; }
    public void setHeadingCounts(Map<String, Integer> headingCounts) { this.headingCounts = headingCounts; }

    public int getTotalLinks() { return totalLinks; }
    public void setTotalLinks(int totalLinks) { this.totalLinks = totalLinks; }

    public int getInternalLinks() { return internalLinks; }
    public void setInternalLinks(int internalLinks) { this.internalLinks = internalLinks; }

    public int getExternalLinks() { return externalLinks; }
    public void setExternalLinks(int externalLinks) { this.externalLinks = externalLinks; }

    public int getImages() { return images; }
    public void setImages(int images) { this.images = images; }

    public int getImagesMissingAlt() { return imagesMissingAlt; }
    public void setImagesMissingAlt(int imagesMissingAlt) { this.imagesMissingAlt = imagesMissingAlt; }

    public int getScripts() { return scripts; }
    public void setScripts(int scripts) { this.scripts = scripts; }

    public int getStylesheets() { return stylesheets; }
    public void setStylesheets(int stylesheets) { this.stylesheets = stylesheets; }

    public int getForms() { return forms; }
    public void setForms(int forms) { this.forms = forms; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public List<String> getTopKeywords() { return topKeywords; }
    public void setTopKeywords(List<String> topKeywords) { this.topKeywords = topKeywords; }

    public boolean isUsesHttps() { return usesHttps; }
    public void setUsesHttps(boolean usesHttps) { this.usesHttps = usesHttps; }

    public boolean isHasViewportMeta() { return hasViewportMeta; }
    public void setHasViewportMeta(boolean hasViewportMeta) { this.hasViewportMeta = hasViewportMeta; }

    public boolean isHasCanonical() { return hasCanonical; }
    public void setHasCanonical(boolean hasCanonical) { this.hasCanonical = hasCanonical; }

    public boolean isHasFavicon() { return hasFavicon; }
    public void setHasFavicon(boolean hasFavicon) { this.hasFavicon = hasFavicon; }

    public String getRobotsTxtUrl() { return robotsTxtUrl; }
    public void setRobotsTxtUrl(String robotsTxtUrl) { this.robotsTxtUrl = robotsTxtUrl; }

    public Integer getRobotsTxtStatus() { return robotsTxtStatus; }
    public void setRobotsTxtStatus(Integer robotsTxtStatus) { this.robotsTxtStatus = robotsTxtStatus; }

    public String getRobotsTxtContent() { return robotsTxtContent; }
    public void setRobotsTxtContent(String robotsTxtContent) { this.robotsTxtContent = robotsTxtContent; }

    public String getRobotsTxtError() { return robotsTxtError; }
    public void setRobotsTxtError(String robotsTxtError) { this.robotsTxtError = robotsTxtError; }
}
