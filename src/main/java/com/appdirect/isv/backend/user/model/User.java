package com.appdirect.isv.backend.user.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.appdirect.isv.backend.user.vo.UserBean;

@Entity
@Table(name = "isv_users")
public class User implements Serializable {
	private static final long serialVersionUID = -7636802416444798779L;

	private Long id;
	private String uuid;
	private String username;
	private String password;
	private String openId;
	private String email;
	private String firstName;
	private String lastName;
	private String zipCode;
	private String timezone;
	private String department;
	private boolean admin = false;
	private Account account;

	public User() {
		super();
	}

	public User(UserBean userBean) {
		super();
		populate(userBean);
	}

	public void populate(UserBean userBean) {
		setUuid(userBean.getUuid());
		setUsername(userBean.getUsername());
		setPassword(userBean.getPassword());
		setOpenId(userBean.getOpenId());
		setEmail(userBean.getEmail());
		setFirstName(userBean.getFirstName());
		setLastName(userBean.getLastName());
		setZipCode(userBean.getZipCode());
		setDepartment(userBean.getDepartment());
		setTimezone(userBean.getTimezone());
		setAdmin(userBean.isAdmin());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "uuid", unique = true)
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Column(name = "username", unique = true)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "open_id", unique = true)
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	@Column(name = "email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "first_name")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "last_name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "zip_code")
	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Column(name = "timezone")
	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@Column(name = "department")
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@Column(name = "is_admin", nullable = false, columnDefinition = "char(1) default 'N'")
	@Type(type = "yes_no")
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@ManyToOne
	@JoinColumn(name = "account_id")
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
