package com.codebattlearena.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FilePreviewController {

    @GetMapping("/api/materials/preview")
    public ResponseEntity<?> preview(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String path,
            @RequestParam(required = false, name = "name") String filename,
            @RequestParam(required = false) String contentType
    ) throws Exception {
        Source s = resolve(url, path, filename);
        if (s == null) return ResponseEntity.badRequest().body("missing url|path|name");
        String ct = StringUtils.hasText(contentType) ? contentType : s.contentType();
        if (!StringUtils.hasText(ct)) ct = guessFromName(s.filename());
        if (!StringUtils.hasText(ct)) ct = "application/octet-stream";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType(ct));
        h.setContentDisposition(ContentDisposition.inline().filename(encode(s.filename())).build());
        return ResponseEntity.ok().headers(h).body(s.resource());
    }

    @GetMapping("/api/materials/download")
    public ResponseEntity<?> download(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String path,
            @RequestParam(required = false, name = "name") String filename,
            @RequestParam(required = false) String contentType
    ) throws Exception {
        Source s = resolve(url, path, filename);
        if (s == null) return ResponseEntity.badRequest().body("missing url|path|name");
        String ct = StringUtils.hasText(contentType) ? contentType : s.contentType();
        if (!StringUtils.hasText(ct)) ct = guessFromName(s.filename());
        if (!StringUtils.hasText(ct)) ct = "application/octet-stream";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType(ct));
        h.setContentDisposition(ContentDisposition.attachment().filename(encode(s.filename())).build());
        return ResponseEntity.ok().headers(h).body(s.resource());
    }

    private record Source(Resource resource, String contentType, String filename) {}

    private Source resolve(String url, String path, String filename) throws Exception {
        if (hasHttp(url)) {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            String ct = conn.getContentType();
            String name = pickFilename(filename, fromUrlPath(url), null);
            return new Source(new InputStreamResource(conn.getInputStream()), ct, name);
        }
        Path p = resolveLocal(path, filename);
        if (p == null || !Files.exists(p)) return null;
        String ct = Files.probeContentType(p);
        String name = pickFilename(filename, p.getFileName().toString(), null);
        return new Source(new UrlResource(p.toUri()), ct, name);
    }

    private static Path resolveLocal(String path, String filename) {
        if (StringUtils.hasText(path)) {
            if (path.startsWith("file:")) return Paths.get(URI.create(path));
            if (path.startsWith("/")) return Paths.get(path);
            Path up = Paths.get("uploads").resolve(path);
            if (Files.exists(up)) return up;
        }
        if (StringUtils.hasText(filename)) {
            Path up = Paths.get("uploads").resolve(filename);
            if (Files.exists(up)) return up;
        }
        return null;
    }

    private static boolean hasHttp(String s) {
        return s != null && (s.startsWith("http://") || s.startsWith("https://"));
    }

    private static String fromUrlPath(String url) {
        try {
            String p = new URI(url).getPath();
            int i = p.lastIndexOf('/');
            return i >= 0 && i < p.length() - 1 ? p.substring(i + 1) : p;
        } catch (Exception e) {
            return null;
        }
    }

    private static String pickFilename(String prefer, String fallback1, String fallback2) {
        if (StringUtils.hasText(prefer)) return prefer;
        if (StringUtils.hasText(fallback1)) return fallback1;
        if (StringUtils.hasText(fallback2)) return fallback2;
        return "file";
    }

    private static String guessFromName(String filename) {
        try { return java.net.URLConnection.guessContentTypeFromName(filename); }
        catch (Exception e) { return null; }
    }

    private static String encode(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
