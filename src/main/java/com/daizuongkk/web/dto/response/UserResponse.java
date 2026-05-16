package com.daizuongkk.web.dto.response;

import java.util.Date;

import com.daizuongkk.web.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserResponse {
	Long id;
	private String username;
	private String firstName;
	private String lastName;
	private Role role;
	private String avtUrl;
	private String phone;
	private String email;
	private Boolean verified;
	private Boolean phoneVerified;
	private Boolean deleted;
	private String status;
	private Date createdAt;
	private Date updatedAt;

}
