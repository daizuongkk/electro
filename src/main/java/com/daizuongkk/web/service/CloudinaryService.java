package com.daizuongkk.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class CloudinaryService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String uploadImage(Part filePart) throws IOException, InterruptedException {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }

        String cloudName = getCloudName();
        String apiKey = getApiKey();
        String apiSecret = getApiSecret();
        if (cloudName == null || apiKey == null || apiSecret == null) {
            throw new IllegalStateException("Missing Cloudinary config. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET.");
        }

        long timestamp = System.currentTimeMillis() / 1000L;
        String folder = getConfig("CLOUDINARY_FOLDER");
        if (folder == null || folder.isBlank()) {
            folder = "electro/products";
        }

        String signature = sha1("folder=" + folder + "&timestamp=" + timestamp + apiSecret);
        String boundary = "----ElectroCloudinary" + UUID.randomUUID();
        byte[] body = buildMultipartBody(boundary, apiKey, timestamp, signature, folder, filePart);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Cloudinary upload failed: " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode secureUrl = root.get("secure_url");
        if (secureUrl == null || secureUrl.asText().isBlank()) {
            throw new IOException("Cloudinary response missing secure_url");
        }
        return secureUrl.asText();
    }

    private String getCloudName() {
        String cloudName = getConfig("CLOUDINARY_CLOUD_NAME");
        if (cloudName != null && !cloudName.isBlank()) {
            return cloudName;
        }

        String cloudinaryUrl = getConfig("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(cloudinaryUrl);
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private String getApiKey() {
        String apiKey = getConfig("CLOUDINARY_API_KEY");
        if (apiKey != null) {
            return apiKey;
        }
        String[] credentials = getCloudinaryUrlCredentials();
        return credentials == null ? null : credentials[0];
    }

    private String getApiSecret() {
        String apiSecret = getConfig("CLOUDINARY_API_SECRET");
        if (apiSecret != null) {
            return apiSecret;
        }
        String[] credentials = getCloudinaryUrlCredentials();
        return credentials == null ? null : credentials[1];
    }

    private String[] getCloudinaryUrlCredentials() {
        String cloudinaryUrl = getConfig("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            return null;
        }

        try {
            String userInfo = URI.create(cloudinaryUrl).getUserInfo();
            if (userInfo == null || !userInfo.contains(":")) {
                return null;
            }
            String[] parts = userInfo.split(":", 2);
            if (parts[0].isBlank() || parts[1].isBlank()) {
                return null;
            }
            return parts;
        } catch (Exception e) {
            return null;
        }
    }

    private String getConfig(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return value == null || value.isBlank() ? null : value.trim();
    }

    private byte[] buildMultipartBody(
            String boundary,
            String apiKey,
            long timestamp,
            String signature,
            String folder,
            Part filePart
    ) throws IOException {
        String filename = submittedFileName(filePart);
        String contentType = filePart.getContentType() == null ? "application/octet-stream" : filePart.getContentType();

        byte[] fileBytes;
        try (var input = filePart.getInputStream()) {
            fileBytes = input.readAllBytes();
        }

        StringBuilder builder = new StringBuilder();
        appendField(builder, boundary, "api_key", apiKey);
        appendField(builder, boundary, "timestamp", String.valueOf(timestamp));
        appendField(builder, boundary, "signature", signature);
        appendField(builder, boundary, "folder", folder);
        builder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"\r\n")
                .append("Content-Type: ").append(contentType).append("\r\n\r\n");

        byte[] prefix = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] suffix = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[prefix.length + fileBytes.length + suffix.length];
        System.arraycopy(prefix, 0, body, 0, prefix.length);
        System.arraycopy(fileBytes, 0, body, prefix.length, fileBytes.length);
        System.arraycopy(suffix, 0, body, prefix.length + fileBytes.length, suffix.length);
        return body;
    }

    private void appendField(StringBuilder builder, String boundary, String name, String value) {
        builder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n")
                .append(value).append("\r\n");
    }

    private String submittedFileName(Part part) {
        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.isBlank()) {
            return "product-image";
        }
        return submitted.replace("\"", "").replace("\\", "_").replace("/", "_");
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign Cloudinary request", e);
        }
    }
}
