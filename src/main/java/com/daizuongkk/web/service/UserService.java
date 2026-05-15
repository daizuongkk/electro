package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.regex.Pattern;

public class UserService {
	private final UserRepository userRepository = new UserRepository();
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+() .-]{9,20}$");

	public enum UpdateProfileStatus {
		SUCCESS,
		INVALID_INPUT,
		INVALID_USERNAME_FORMAT,
		INVALID_EMAIL_FORMAT,
		INVALID_CURRENT_PASSWORD,
		INVALID_NEW_PASSWORD_FORMAT,
		PASSWORD_MISMATCH,
		USERNAME_EXISTS,
		EMAIL_EXISTS,
		EMAIL_ALREADY_VERIFIED,
		FAILED
	}

	public enum AdminUserSaveStatus {
		SUCCESS,
		INVALID_INPUT,
		INVALID_ID,
		INVALID_USERNAME_FORMAT,
		INVALID_EMAIL_FORMAT,
		INVALID_PHONE_FORMAT,
		INVALID_PASSWORD_FORMAT,
		INVALID_ROLE,
		INVALID_STATUS,
		USERNAME_EXISTS,
		EMAIL_EXISTS,
		FAILED
	}

	public UserResponse findById(Long id) {

		return userToUserResponse(userRepository.findById(id));
	}

	public List<UserResponse> findUsers(AdminUserSearchRequest filters, int page, int size) {
		return userRepository.findPage(filters, page, size)
				.stream()
				.map(this::userToUserResponse)
				.toList();
	}

	public Long countUsers(AdminUserSearchRequest filters) {
		return userRepository.count(filters);
	}

	public boolean updateStatus(Long id, String status) {
		return userRepository.updateStatus(id, status);
	}

	public boolean createUser(User user, String rawPassword) {
		if (user == null || rawPassword == null || rawPassword.isBlank()) {
			return false;
		}
		user.setPassword(hashPassword(rawPassword));
		return userRepository.create(user);
	}

	public boolean updateUser(User user, String rawPassword) {
		if (user == null) {
			return false;
		}
		boolean updatePassword = rawPassword != null && !rawPassword.isBlank();
		if (updatePassword) {
			user.setPassword(hashPassword(rawPassword));
		}
		return userRepository.update(user, updatePassword);
	}

	public AdminUserSaveStatus saveAdminUser(User user, String rawPassword) {
		if (user == null) {
			return AdminUserSaveStatus.INVALID_INPUT;
		}

		boolean creating = user.getId() == null;
		if (!creating && user.getId() <= 0) {
			return AdminUserSaveStatus.INVALID_ID;
		}

		String username = normalizeOptional(user.getUsername());
		String email = normalizeEmail(user.getEmail());
		String phone = normalizeOptional(user.getPhone());
		String status = normalizeStatus(user.getStatus());

		if (!hasText(username) || !hasText(email)) {
			return AdminUserSaveStatus.INVALID_INPUT;
		}
		if (!AuthService.isValidUsernameFormat(username)) {
			return AdminUserSaveStatus.INVALID_USERNAME_FORMAT;
		}
		if (!AuthService.isValidEmailFormat(email)) {
			return AdminUserSaveStatus.INVALID_EMAIL_FORMAT;
		}
		if (hasText(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
			return AdminUserSaveStatus.INVALID_PHONE_FORMAT;
		}
		if (user.getRole() == null) {
			return AdminUserSaveStatus.INVALID_ROLE;
		}
		if (status == null) {
			return AdminUserSaveStatus.INVALID_STATUS;
		}

		boolean updatePassword = hasText(rawPassword);
		if (creating && !updatePassword) {
			return AdminUserSaveStatus.INVALID_PASSWORD_FORMAT;
		}
		if (updatePassword && !AuthService.isValidPasswordFormat(rawPassword)) {
			return AdminUserSaveStatus.INVALID_PASSWORD_FORMAT;
		}

		if (creating ? userRepository.existsByUsername(username) : userRepository.existsByUsernameExceptId(username, user.getId())) {
			return AdminUserSaveStatus.USERNAME_EXISTS;
		}
		if (creating ? userRepository.existsByEmail(email) : userRepository.existsByEmailExceptId(email, user.getId())) {
			return AdminUserSaveStatus.EMAIL_EXISTS;
		}

		user.setUsername(username);
		user.setEmail(email);
		user.setFirstName(normalizeOptional(user.getFirstName()));
		user.setLastName(normalizeOptional(user.getLastName()));
		user.setPhone(phone);
		user.setStatus(status);
		user.setVerified(user.getVerified() != null && user.getVerified());
		if (updatePassword) {
			user.setPassword(hashPassword(rawPassword));
		}

		boolean saved = creating
				? userRepository.create(user)
				: userRepository.update(user, updatePassword);

		return saved ? AdminUserSaveStatus.SUCCESS : AdminUserSaveStatus.FAILED;
	}

	public boolean deleteUser(Long id) {
		return userRepository.deleteById(id);
	}

	public UpdateProfileStatus updateProfile(Long userId,
			String username,
			String email,
			String firstName,
			String lastName,
			String phone,
			String avtUrl) {
		if (userId == null || userId <= 0 || email == null) {
			return UpdateProfileStatus.INVALID_INPUT;
		}

		User user = userRepository.findById(userId);
		if (user == null) {
			return UpdateProfileStatus.INVALID_INPUT;
		}

		String normalizedEmail = Boolean.TRUE.equals(user.getVerified())
				? user.getEmail()
				: email.trim().toLowerCase();
		if (normalizedEmail.isEmpty()) {
			return UpdateProfileStatus.INVALID_INPUT;
		}

		if (Boolean.TRUE.equals(user.getVerified()) && !user.getEmail().equalsIgnoreCase(email.trim())) {
			return UpdateProfileStatus.EMAIL_ALREADY_VERIFIED;
		}

		if (!AuthService.isValidEmailFormat(normalizedEmail)) {
			return UpdateProfileStatus.INVALID_EMAIL_FORMAT;
		}

		User userWithEmail = userRepository.findByUsernameOrEmail(normalizedEmail);
		if (userWithEmail != null && !userWithEmail.getId().equals(userId)
				&& normalizedEmail.equalsIgnoreCase(userWithEmail.getEmail())) {
			return UpdateProfileStatus.EMAIL_EXISTS;
		}

		user.setUsername(user.getUsername());
		user.setEmail(normalizedEmail);
		user.setFirstName(normalizeOptional(firstName));
		user.setLastName(normalizeOptional(lastName));
		user.setPhone(normalizeOptional(phone));
		user.setAvtUrl(normalizeOptional(avtUrl));

		return userRepository.updateProfile(user, null) ? UpdateProfileStatus.SUCCESS : UpdateProfileStatus.FAILED;
	}

	public UpdateProfileStatus changePassword(Long userId,
			String currentPassword,
			String newPassword,
			String confirmPassword) {
		if (userId == null || userId <= 0) {
			return UpdateProfileStatus.INVALID_INPUT;
		}

		User user = userRepository.findById(userId);
		if (user == null) {
			return UpdateProfileStatus.INVALID_INPUT;
		}

		if (!hasText(currentPassword) || !matchesPassword(currentPassword, user.getPassword())) {
			return UpdateProfileStatus.INVALID_CURRENT_PASSWORD;
		}

		if (!AuthService.isValidPasswordFormat(newPassword)) {
			return UpdateProfileStatus.INVALID_NEW_PASSWORD_FORMAT;
		}

		if (!newPassword.equals(confirmPassword)) {
			return UpdateProfileStatus.PASSWORD_MISMATCH;
		}

		return userRepository.updatePassword(userId, hashPassword(newPassword))
				? UpdateProfileStatus.SUCCESS
				: UpdateProfileStatus.FAILED;
	}

	public User getUserModelById(Long id) {
		return userRepository.findById(id);
	}

	public User getAdminUserModelById(Long id) {
		return userRepository.findByIdIncludingDeleted(id);
	}

	public boolean usernameExists(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	public UserResponse toUserResponse(User user) {
		return userToUserResponse(user);
	}

	private String hashPassword(String rawPassword) {
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
	}

	private boolean matchesPassword(String rawPassword, String storedPassword) {
		if (storedPassword == null) {
			return false;
		}

		if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
			return BCrypt.checkpw(rawPassword, storedPassword);
		}

		return storedPassword.equals(rawPassword);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String normalizeOptional(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeEmail(String value) {
		String normalized = normalizeOptional(value);
		return normalized == null ? null : normalized.toLowerCase();
	}

	private String normalizeStatus(String value) {
		String status = value == null ? "" : value.trim().toUpperCase();
		return status.equals("ACTIVE") || status.equals("INACTIVE") || status.equals("BANNED") ? status : null;
	}

	private UserResponse userToUserResponse(User user) {
		if (user == null) {
			return null;
		}

		return UserResponse.builder()
				.id(user.getId())
				.username(user.getUsername())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.role(user.getRole())
				.avtUrl(user.getAvtUrl())
				.phone(user.getPhone())
				.email(user.getEmail())
				.verified(user.getVerified())
				.phoneVerified(user.getPhoneVerified())
				.deleted(user.getDeleted())
				.status(user.getStatus())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.build();
	}
}
