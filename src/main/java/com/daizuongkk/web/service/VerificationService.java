package com.daizuongkk.web.service;

import java.security.SecureRandom;

import com.daizuongkk.web.model.User;
import com.daizuongkk.web.model.VerificationChannel;
import com.daizuongkk.web.repository.UserRepository;
import com.daizuongkk.web.repository.VerificationOtpRepository;

public class VerificationService {
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final int OTP_TTL_MINUTES = 10;

	private final UserRepository userRepository;
	private final VerificationOtpRepository otpRepository;
	private final EmailService emailService;
	private final SmsService smsService;

	public VerificationService() {
		this(new UserRepository(), new VerificationOtpRepository(), new EmailService(), new SmsService());
	}

	public VerificationService(UserRepository userRepository,
			VerificationOtpRepository otpRepository,
			EmailService emailService,
			SmsService smsService) {
		this.userRepository = userRepository;
		this.otpRepository = otpRepository;
		this.emailService = emailService;
		this.smsService = smsService;
	}

	public VerificationStatus sendOtp(Long userId, VerificationChannel channel) {
		User user = userRepository.findById(userId);
		return sendOtp(userId, channel, getTarget(user, channel));
	}

	public VerificationStatus sendOtp(Long userId, VerificationChannel channel, String targetValue) {
		User user = userRepository.findById(userId);
		if (user == null || channel == null) {
			return VerificationStatus.INVALID_INPUT;
		}

		String target = normalizeTarget(channel, targetValue);
		if (target == null || target.isBlank()) {
			return VerificationStatus.MISSING_TARGET;
		}
		VerificationStatus validationStatus = validateTarget(userId, channel, target);
		if (validationStatus != VerificationStatus.OTP_SENT) {
			return validationStatus;
		}

		String otp = generateOtp();
		if (!otpRepository.create(
				userId,
				channel,
				target,
				otp)) {
			return VerificationStatus.FAILED;
		}

		try {
			if (channel == VerificationChannel.EMAIL) {
				emailService.sendVerificationOtp(target, otp);
			} else {
				smsService.sendVerificationOtp(target, otp);
			}
			return VerificationStatus.OTP_SENT;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return VerificationStatus.SEND_FAILED;
		}
	}

	public VerificationStatus verifyOtp(Long userId, VerificationChannel channel, String otp) {
		User user = userRepository.findById(userId);
		return verifyOtp(userId, channel, getTarget(user, channel), otp);
	}

	public VerificationStatus verifyOtp(Long userId, VerificationChannel channel, String targetValue, String otp) {
		User user = userRepository.findById(userId);
		String normalizedOtp = normalizeOtp(otp);
		if (user == null || channel == null || normalizedOtp == null) {
			return VerificationStatus.INVALID_INPUT;
		}

		String target = normalizeTarget(channel, targetValue);
		if (target == null || target.isBlank()) {
			return VerificationStatus.MISSING_TARGET;
		}
		VerificationStatus validationStatus = validateTarget(userId, channel, target);
		if (validationStatus != VerificationStatus.OTP_SENT) {
			return validationStatus;
		}

		VerificationOtpRepository.VerifyResult result = otpRepository.verify(userId, channel, target, normalizedOtp);
		if (result == VerificationOtpRepository.VerifyResult.SUCCESS) {
			boolean updated = channel == VerificationChannel.EMAIL
					? userRepository.updateEmailAndVerified(userId, target)
					: userRepository.updatePhoneAndVerified(userId, target);
			return updated ? VerificationStatus.VERIFIED : VerificationStatus.FAILED;
		}
		if (result == VerificationOtpRepository.VerifyResult.EXPIRED_OR_MISSING) {
			return VerificationStatus.OTP_EXPIRED;
		}
		if (result == VerificationOtpRepository.VerifyResult.TOO_MANY_ATTEMPTS) {
			return VerificationStatus.TOO_MANY_ATTEMPTS;
		}
		if (result == VerificationOtpRepository.VerifyResult.INVALID) {
			return VerificationStatus.INVALID_OTP;
		}
		return VerificationStatus.FAILED;
	}

	private String getTarget(User user, VerificationChannel channel) {
		if (user == null || channel == null) {
			return null;
		}
		return channel == VerificationChannel.EMAIL ? user.getEmail() : normalizePhone(user.getPhone());
	}

	private String normalizeTarget(VerificationChannel channel, String targetValue) {
		if (targetValue == null || channel == null) {
			return null;
		}
		if (channel == VerificationChannel.EMAIL) {
			return targetValue.trim().toLowerCase();
		}
		return normalizePhone(targetValue);
	}

	private VerificationStatus validateTarget(Long userId, VerificationChannel channel, String target) {
		if (channel == VerificationChannel.EMAIL) {
			if (!AuthService.isValidEmailFormat(target)) {
				return VerificationStatus.INVALID_EMAIL;
			}
			User existing = userRepository.findByUsernameOrEmail(target);
			if (existing != null && !existing.getId().equals(userId)) {
				return VerificationStatus.EMAIL_EXISTS;
			}
		}
		return VerificationStatus.OTP_SENT;
	}

	private String normalizePhone(String phone) {
		if (phone == null) {
			return null;
		}
		String normalized = phone.trim()
				.replace(" ", "")
				.replace("-", "")
				.replace(".", "");
		if (normalized.startsWith("0")) {
			return "+84" + normalized.substring(1);
		}
		if (!normalized.startsWith("+") && normalized.matches("\\d+")) {
			return "+" + normalized;
		}
		return normalized;
	}

	private String normalizeOtp(String otp) {
		if (otp == null) {
			return null;
		}

		String normalized = otp.trim().replaceAll("\\D", "");
		return normalized.length() == 6 ? normalized : null;
	}

	private String generateOtp() {
		return String.format("%06d", RANDOM.nextInt(1_000_000));
	}

	public enum VerificationStatus {
		OTP_SENT,
		VERIFIED,
		INVALID_INPUT,
		MISSING_TARGET,
		SEND_FAILED,
		INVALID_OTP,
		OTP_EXPIRED,
		TOO_MANY_ATTEMPTS,
		INVALID_EMAIL,
		EMAIL_EXISTS,
		FAILED
	}
}
