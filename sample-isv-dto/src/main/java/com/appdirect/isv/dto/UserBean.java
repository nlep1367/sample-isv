package com.appdirect.isv.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.appdirect.isv.model.User;

@Data @NoArgsConstructor
public class UserBean implements Serializable {
	private static final long serialVersionUID = -8955516091357057444L;

	private Long id;
	private String appDirectUuid;
	private String appDirectOpenId;
	private String email;
	private String firstName;
	private String lastName;
	private String zipCode;
	private String department;
	private String timezone;
	private boolean admin = false;

	public UserBean(User user) {
		this.id = user.getId();
		this.appDirectUuid = user.getAppDirectUuid();
		this.appDirectOpenId = user.getAppDirectOpenId();
		this.email = user.getEmail();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.zipCode = user.getZipCode();
		this.department = user.getDepartment();
		this.timezone = user.getTimezone();
		this.admin = user.isAdmin();
	}

	public User toUser() {
		User user = new User();
		user.setAppDirectUuid(getAppDirectUuid());
		user.setAppDirectOpenId(getAppDirectOpenId());
		user.setEmail(getEmail());
		user.setFirstName(getFirstName());
		user.setLastName(getLastName());
		user.setZipCode(getZipCode());
		user.setDepartment(getDepartment());
		user.setTimezone(getTimezone());
		user.setAdmin(isAdmin());
		return user;
	}
}
