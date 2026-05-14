package com.daizuongkk.web.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

	public void sendVerificationOtp(String toPhone, String otp) {
		String accountSid = getRequiredConfig("TWILIO_ACCOUNT_SID");
		String authToken = getRequiredConfig("TWILIO_AUTH_TOKEN");
		String fromPhone = getRequiredConfig("TWILIO_FROM_PHONE");

		Twilio.init(accountSid, authToken);
		Message.creator(
				new PhoneNumber(toPhone),
				new PhoneNumber(fromPhone),
				"Ma OTP xac minh so dien thoai Electro cua ban la: " + otp + ". Ma co hieu luc trong 10 phut.")
				.create();
	}

	private String getRequiredConfig(String key) {
		String value = getConfig(key);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing SMS config: " + key);
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
