package com.appdirect.isv.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.appdirect.isv.model.Account;
import com.appdirect.isv.model.Addon;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.model.User;
import com.google.common.base.Preconditions;

@Data @NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AccountBean implements Serializable {
	private static final long serialVersionUID = 2991393185869834690L;

	private Long id;
	private String uuid;
	private String appDirectBaseUrl;
	private String editionCode;
	private Integer maxUsers = null;
	private String samlIdpEntityId;
	private String samlIdpMetadataUrl;
	private ApplicationProfile applicationProfile;
	private List<UserBean> users = new ArrayList<>();
	private List<AddonBean> addons = new ArrayList<>();

	public AccountBean(ApplicationProfile applicationProfile) {
		super();
		Preconditions.checkNotNull(applicationProfile);
		this.applicationProfile = applicationProfile;
	}

	public AccountBean(Account account) {
		super();
		this.id = account.getId();
		this.uuid = account.getUuid();
		this.appDirectBaseUrl = account.getAppDirectBaseUrl();
		this.editionCode = account.getEditionCode();
		this.maxUsers = account.getMaxUsers();
		this.samlIdpEntityId = account.getSamlIdpEntityId();
		this.samlIdpMetadataUrl = account.getSamlIdpMetadataUrl();
		this.applicationProfile = account.getApplicationProfile();
		for (User user : account.getUsers()) {
			this.users.add(new UserBean(user));
		}
		for (Addon addon : account.getAddons()) {
			this.addons.add(new AddonBean(addon));
		}
	}

	public Account toAccount() {
		Account account = new Account(applicationProfile);
		account.setUuid(getUuid());
		account.setEditionCode(getEditionCode());
		account.setMaxUsers(getMaxUsers());
		account.setAppDirectBaseUrl(getAppDirectBaseUrl());
		account.setSamlIdpEntityId(getSamlIdpEntityId());
		account.setSamlIdpMetadataUrl(getSamlIdpMetadataUrl());
		return account;
	}
}
