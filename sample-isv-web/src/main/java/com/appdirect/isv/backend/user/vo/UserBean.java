package com.appdirect.isv.backend.user.vo;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.appdirect.isv.backend.security.vo.Role;
import com.appdirect.isv.backend.user.model.User;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "user")
@Data @NoArgsConstructor
public class UserBean implements UserDetails {
	private static final long serialVersionUID = -8955516091357057444L;

	@XmlTransient
	private Long id;
	private String uuid;
	private String openId;
	private String email;
	private String firstName;
	private String lastName;
	private String zipCode;
	private String department;
	private String timezone;
	private boolean admin = false;

	public UserBean(User user) {
		this.id = user.getId();
		this.uuid = user.getUuid();
		this.openId = user.getOpenId();
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
		user.setUuid(getUuid());
		user.setUsername(getUsername());
		user.setPassword(getPassword());
		user.setOpenId(getOpenId());
		user.setEmail(getEmail());
		user.setFirstName(getFirstName());
		user.setLastName(getLastName());
		user.setZipCode(getZipCode());
		user.setDepartment(getDepartment());
		user.setTimezone(getTimezone());
		user.setAdmin(isAdmin());
		return user;
	}

	@Override
	public String getUsername() {
		return uuid;
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return Collections.singleton((GrantedAuthority) Role.USER);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
