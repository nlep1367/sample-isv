package com.appdirect.isv.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Getter @Setter @NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "isv_accounts")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "uuid")
	private String appDirectUuid;

	@Column(name = "appdirect_base_url", length = 1000)
	private String appDirectBaseUrl;

	@Column(name = "edition_code")
	private String editionCode;

	@Column(name = "max_users")
	private Integer maxUsers = null;

	@Column(name = "saml_idp_entity_id", unique = true, length = 255)
	private String samlIdpEntityId;

	@Column(name = "saml_idp_metadata_url", length = 255)
	private String samlIdpMetadataUrl;

	@ManyToOne
	@JoinColumn(name = "application_profile_id")
	private ApplicationProfile applicationProfile;

	@OneToMany(mappedBy = "account", orphanRemoval = true, cascade = { CascadeType.ALL })
	private List<User> users = Lists.newArrayList();

	@OneToMany(mappedBy = "account", orphanRemoval = true, cascade = { CascadeType.ALL })
	private List<Addon> addons = Lists.newArrayList();

	public Account(ApplicationProfile applicationProfile) {
		this();
		Preconditions.checkNotNull(applicationProfile);
		this.applicationProfile = applicationProfile;
	}

	public void addUser(User user) {
		users.add(user);
		user.setAccount(this);
	}

	public void addAddon(Addon addon) {
		addons.add(addon);
		addon.setAccount(this);
	}
}
