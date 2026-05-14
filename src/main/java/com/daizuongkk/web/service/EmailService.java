package com.daizuongkk.web.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

	public void sendVerificationOtp(String toEmail, String otp) {
		String username = getRequiredConfig("GMAIL_SMTP_USERNAME");
		String password = getRequiredConfig("GMAIL_SMTP_APP_PASSWORD");
		String from = getConfig("GMAIL_SMTP_FROM");
		if (from == null || from.isBlank()) {
			from = username;
		}

		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");

		Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject("Mã xác minh tài khoản Electro");
			message.setText("""
					Mã OTP xác minh tài khoản Electro của bạn là: %s

					Mã có hiệu lực trong 10 phút. Không chia sẻ với bất kì ai!
					""".formatted(otp));
			Transport.send(message);
		} catch (MessagingException e) {
			throw new IllegalStateException("Không thể gửi email xác minh qua Gmail SMTP.", e);
		}
	}

	private String getRequiredConfig(String key) {
		String value = getConfig(key);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing email config: " + key);
		}
		return value;
	}

	private String getConfig(String key) {
		String value = System.getenv(key);
		if (value == null || value.isBlank()) {
			value = System.getProperty(key);
		}
		return value == null ? null : value.trim();
	}
}
