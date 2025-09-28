package com.codebattlearena.controller;

import com.codebattlearena.model.Material;
import com.codebattlearena.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.PathVariable;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialRepository materialRepository;

    @GetMapping("/api/materials/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable Long id) throws Exception {
        Material m = materialRepository.findById(id).orElse(null);
        if (m == null) return ResponseEntity.notFound().build();

        Source s = resolveSource(m);
        if (s == null) return ResponseEntity.notFound().build();

        String filename = s.filename;
        String contentType = s.contentType != null ? s.contentType : guessContentType(filename);
        if (contentType == null) contentType = "application/octet-stream";

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType(contentType));
        h.setContentDisposition(ContentDisposition.inline().filename(encode(filename)).build());
        return ResponseEntity.ok().headers(h).body(s.resource);
    }

    @GetMapping("/api/materials/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) throws Exception {
        Material m = materialRepository.findById(id).orElse(null);
        if (m == null) return ResponseEntity.notFound().build();

        Source s = resolveSource(m);
        if (s == null) return ResponseEntity.notFound().build();

        String filename = s.filename;
        String contentType = s.contentType != null ? s.contentType : guessContentType(filename);
        if (contentType == null) contentType = "application/octet-stream";

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType(contentType));
        h.setContentDisposition(ContentDisposition.attachment().filename(encode(filename)).build());
        return ResponseEntity.ok().headers(h).body(s.resource);
    }

    private record Source(Resource resource, String contentType, String filename) {}

    private Source resolveSource(Material m) throws Exception {
        String url = str(get(m, "url"));
        String storedPath = str(get(m, "storedPath"));
        String originalName = str(get(m, "originalFilename"));
        String title = str(get(m, "title"));

        if (hasHttp(url)) {
            URL u = new URL(url);
            HttpURLConnection head = (HttpURLConnection) u.openConnection();
            head.setInstanceFollowRedirects(true);
            head.setRequestMethod("GET");
            head.setConnectTimeout(5000);
            head.setReadTimeout(10000);
            String ct = head.getContentType();
            String fname = filenameFrom(url, originalName, title);
            InputStreamResource r = new InputStreamResource(head.getInputStream());
            return new Source(r, ct, fname);
        }

        Path p = resolveLocalPath(storedPath, originalName, title);
        if (p == null || !Files.exists(p)) return null;
        Resource r = new UrlResource(p.toUri());
        String ct = Files.probeContentType(p);
        String fname = filenameFrom(p.getFileName().toString(), originalName, title);
        return new Source(r, ct, fname);
    }

    private static Path resolveLocalPath(String storedPath, String originalName, String title) {
        if (StringUtils.hasText(storedPath)) {
            if (storedPath.startsWith("file:")) {
                return Paths.get(URI.create(storedPath));
            } else if (storedPath.startsWith("/")) {
                return Paths.get(storedPath);
            } else {
                Path up = Paths.get("uploads").resolve(storedPath);
                if (Files.exists(up)) return up;
            }
        }
        if (StringUtils.hasText(originalName)) {
            Path up = Paths.get("uploads").resolve(originalName);
            if (Files.exists(up)) return up;
        }
        if (StringUtils.hasText(title)) {
            Path up = Paths.get("uploads").resolve(title);
            if (Files.exists(up)) return up;
        }
        Path cp = Paths.get("src/main/resources/uploads");
        if (Files.exists(cp)) return cp;
        return null;
    }

    private static String filenameFrom(String prefer, String originalName, String title) {
        if (StringUtils.hasText(originalName)) return originalName;
        if (StringUtils.hasText(prefer)) {
            int i = prefer.lastIndexOf('/');
            return i >= 0 && i < prefer.length() - 1 ? prefer.substring(i + 1) : prefer;
        }
        if (StringUtils.hasText(title)) return title;
        return "file";
    }

    private static boolean hasHttp(String s) {
        return s != null && (s.startsWith("http://") || s.startsWith("https://"));
    }

    private static Object get(Object bean, String field) {
        try {
            var f = bean.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(bean);
        } catch (Exception ignore) {
            try {
                var m = bean.getClass().getMethod("get" + StringUtils.capitalize(field));
                return m.invoke(bean);
            } catch (Exception ignore2) {
                return null;
            }
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String guessContentType(String filename) {
        String ct = URLConnectionGuess(filename);
        return ct != null ? ct : null;
    }

    private static String URLConnectionGuess(String filename) {
        try {
            return java.net.URLConnection.guessContentTypeFromName(filename);
        } catch (Exception e) {
            return null;
        }
    }

    private static String encode(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
