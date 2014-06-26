package com.appdirect.isv.backend.user.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.appdirect.isv.backend.user.vo.AccountBean;

@Entity
@Table(name = "isv_accounts")
public class Account implements Serializable {
	private static final long serialVersionUID = -4290349667150930479L;

	private Long id;
	private String uuid;
	private String name;
	private List<User> users = new ArrayList<User>();
	private String editionCode;
	private Integer maxUsers = null;
	private boolean appDirectManaged = false;
	private String appDirectBaseUrl;
	private List<Addon> addons = new ArrayList<Addon>();

	public Account() {
		super();
	}

	public Account(AccountBean accountBean) {
		populate(accountBean);
	}

	public void populate(AccountBean accountBean) {
		setUuid(accountBean.getUuid());
		setName(accountBean.getName());
		setEditionCode(accountBean.getEditionCode());
		setMaxUsers(accountBean.getMaxUsers());
		setAppDirectBaseUrl(accountBean.getAppDirectBaseUrl());
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

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy = "account", orphanRemoval = true, cascade = { CascadeType.ALL })
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	@Column(name = "edition_code")
	public String getEditionCode() {
		return editionCode;
	}

	public void setEditionCode(String editionCode) {
		this.editionCode = editionCode;
	}

	@Column(name = "max_users")
	public Integer getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(Integer maxUsers) {
		this.maxUsers = maxUsers;
	}

	@Column(name = "is_appdirect_managed", nullable = false, columnDefinition = "char(1) default 'N'")
	@Type(type = "yes_no")
	public boolean isAppDirectManaged() {
		return appDirectManaged;
	}

	public void setAppDirectManaged(boolean appDirectManaged) {
		this.appDirectManaged = appDirectManaged;
	}

	@Column(name = "appdirect_base_url", length = 1000)
	public String getAppDirectBaseUrl() {
		return appDirectBaseUrl;
	}

	public void setAppDirectBaseUrl(String appDirectBaseUrl) {
		this.appDirectBaseUrl = appDirectBaseUrl;
	}

	@OneToMany(mappedBy = "account", orphanRemoval = true, cascade = { CascadeType.ALL })
	public List<Addon> getAddons() {
		return addons;
	}

	public void setAddons(List<Addon> addons) {
		this.addons = addons;
	}
}
