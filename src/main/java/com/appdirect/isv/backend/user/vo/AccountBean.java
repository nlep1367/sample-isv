package com.appdirect.isv.backend.user.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;

import com.appdirect.isv.backend.user.model.Account;
import com.appdirect.isv.backend.user.model.Addon;
import com.appdirect.isv.backend.user.model.User;

@Data
@XmlRootElement(name = "account")
public class AccountBean implements Serializable {
	private static final long serialVersionUID = 2991393185869834690L;

	private Long id;
	private String uuid;
	private String name;
	private List<UserBean> users = new ArrayList<UserBean>();
	private String editionCode;
	private Integer maxUsers = null;
	private boolean appDirectManaged = false;
	private String appDirectBaseUrl;
	private List<AddonBean> addons = new ArrayList<AddonBean>();

	public AccountBean() {
		super();
	}

	public AccountBean(Account account) {
		this.id = account.getId();
		this.uuid = account.getUuid();
		this.name = account.getName();
		for (User user : account.getUsers()) {
			this.users.add(new UserBean(user));
		}
		for (Addon addon : account.getAddons()) {
			this.addons.add(new AddonBean(addon));
		}
		this.editionCode = account.getEditionCode();
		this.maxUsers = account.getMaxUsers();
		this.appDirectManaged = account.isAppDirectManaged();
		this.appDirectBaseUrl = account.getAppDirectBaseUrl();
	}

	@XmlTransient
	public Long getId() {
		return id;
	}
}
