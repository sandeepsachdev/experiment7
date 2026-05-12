# Website Analyzer

A Spring Boot web app that takes a website URL and shows basic analysis: HTTP
response details, metadata, heading structure, link/image counts, SEO checks,
word count, and top keywords.

## Run locally

```bash
mvn spring-boot:run
```

Then open http://localhost:8080.

## Build a JAR

```bash
mvn clean package
java -jar target/website-analyzer-0.0.1-SNAPSHOT.jar
```

## Docker

```bash
docker build -t website-analyzer .
docker run --rm -p 8080:8080 website-analyzer
```

## Deploy

The app reads the `PORT` environment variable and binds to `0.0.0.0`, so it
works out of the box on platforms that inject a port.

### Render

1. Push this repo to GitHub.
2. In Render, create a new **Web Service** and point it at the repo.
3. Choose **Docker** as the environment. No further config is required &mdash;
   Render sets `PORT` automatically and the Dockerfile listens on it.

### Railway

1. Push this repo to GitHub.
2. In Railway, create a new project from the repo. Railway detects the
   `Dockerfile` and builds the image.
3. Railway injects `PORT`; the app picks it up automatically.

## What it analyzes

- HTTP status code, response time, content size, server, content type
- Page title, meta description, meta keywords, language, charset
- Headings (H1&ndash;H6), links (internal/external), images (and missing alt
  attributes), scripts, stylesheets, forms
- SEO checks: HTTPS, viewport meta, canonical link, favicon
- Word count and top 10 keywords (with stop words filtered out)
