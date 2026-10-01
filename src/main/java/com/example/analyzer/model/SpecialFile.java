package com.example.analyzer.model;

public class SpecialFile {

    private String name;
    private String path;
    private String url;
    private Integer status;
    private String infoUrl;
    private String description;
    private String error;

    public SpecialFile() {}

    public SpecialFile(String name, String path, String infoUrl, String description) {
        this.name = name;
        this.path = path;
        this.infoUrl = infoUrl;
        this.description = description;
    }

    public boolean isFound() {
        return status != null && status >= 200 && status < 300;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getInfoUrl() { return infoUrl; }
    public void setInfoUrl(String infoUrl) { this.infoUrl = infoUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
