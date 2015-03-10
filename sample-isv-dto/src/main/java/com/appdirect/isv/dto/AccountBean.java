package com.appdirect.isv.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.appdirect.isv.model.Account;
import com.appdirect.isv.model.Addon;
import com.appdirect.isv.model.User;

@Data @NoArgsConstructor
public class AccountBean implements Serializable {
	private static final long serialVersionUID = 2991393185869834690L;

	private Long id;
	private String uuid;
	private String appDirectBaseUrl;
	private String editionCode;
	private Integer maxUsers = null;
	private List<UserBean> users = new ArrayList<UserBean>();
	private List<AddonBean> addons = new ArrayList<AddonBean>();

	public AccountBean(Account account) {
		this.id = account.getId();
		this.uuid = account.getUuid();
		this.appDirectBaseUrl = account.getAppDirectBaseUrl();
		this.editionCode = account.getEditionCode();
		this.maxUsers = account.getMaxUsers();
		for (User user : account.getUsers()) {
			this.users.add(new UserBean(user));
		}
		for (Addon addon : account.getAddons()) {
			this.addons.add(new AddonBean(addon));
		}
	}

	public Account toAccount() {
		Account account = new Account();
		account.setUuid(getUuid());
		account.setEditionCode(getEditionCode());
		account.setMaxUsers(getMaxUsers());
		account.setAppDirectBaseUrl(getAppDirectBaseUrl());
		return account;
	}
}
