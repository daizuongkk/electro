package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.repository.UserRepository;

public class UserService {
	private UserRepository userRepository = new UserRepository();

	public UserResponse findById(Long id) {

		return userToUserResponse(userRepository.findById(id));
	}

	private UserResponse userToUserResponse(User user) {
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
				.build();
	}
}
