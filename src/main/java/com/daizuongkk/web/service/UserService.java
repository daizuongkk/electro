package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {
	private UserRepository userRepository = new UserRepository();

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

	public User getUserModelById(Long id) {
		return userRepository.findById(id);
	}

	public boolean usernameExists(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	private String hashPassword(String rawPassword) {
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
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
				.status(user.getStatus())
				.build();
	}
}
