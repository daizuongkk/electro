package com.daizuongkk.web.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.Map;

public class CloudinaryService {

	private final Cloudinary cloudinary;

	public CloudinaryService() {
		String cloudinaryUrl = getConfig("CLOUDINARY_URL");

		if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
			this.cloudinary = new Cloudinary(cloudinaryUrl);
		} else {
			this.cloudinary = new Cloudinary(ObjectUtils.asMap(
					"cloud_name", getRequiredConfig("CLOUDINARY_CLOUD_NAME"),
					"api_key", getRequiredConfig("CLOUDINARY_API_KEY"),
					"api_secret", getRequiredConfig("CLOUDINARY_API_SECRET")));
		}

		this.cloudinary.config.secure = true;
	}

	public String uploadImage(Part filePart) throws IOException {
		if (filePart == null || filePart.getSize() <= 0) {
			return null;
		}

		return uploadImage(filePart, getUploadFolder("electro/products"));
	}

	public String uploadImage(Part filePart, String folder) throws IOException {
		if (filePart == null || filePart.getSize() <= 0) {
			return null;
		}

		byte[] fileBytes;
		try (var inputStream = filePart.getInputStream()) {
			fileBytes = inputStream.readAllBytes();
		}

		Map<?, ?> uploadResult = cloudinary.uploader().upload(
				fileBytes,
				ObjectUtils.asMap(
						"folder", folder,
						"resource_type", "image"));

		Object secureUrl = uploadResult.get("secure_url");

		if (secureUrl == null || secureUrl.toString().isBlank()) {
			throw new IOException("Cloudinary response missing secure_url");
		}

		return secureUrl.toString();
	}

	private String getUploadFolder(String defaultFolder) {
		String folder = getConfig("CLOUDINARY_FOLDER");
		return folder == null || folder.isBlank() ? defaultFolder : folder;
	}

	private String getRequiredConfig(String key) {
		String value = getConfig(key);

		if (value == null || value.isBlank()) {
			throw new IllegalStateException(
					"Missing Cloudinary config: " + key);
		}

		return value;
	}

	private String getConfig(String key) {
		String value = System.getenv(key);

		if (value == null || value.isBlank()) {
			value = System.getProperty(key);
		}

		return value == null || value.isBlank() ? null : value.trim();
	}
}
