package com.appdirect.isv.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Type;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "isv_application_profiles")
public class ApplicationProfile extends TimestampedObject implements Serializable {
	private static final long serialVersionUID = 8749509893827875016L;

	@Id
	@Column(name = "uuid", length = 36)
	private String uuid;

	@Column(name = "url")
	private String url;

	@Column(name = "oauth_consumer_key", unique = true, nullable = false)
	private String oauthConsumerKey;

	@Column(name = "oauth_consumer_secret")
	private String oauthConsumerSecret;

	@Column(name = "authentication_method", nullable = false)
	@Enumerated(EnumType.STRING)
	private AuthenticationMethod authenticationMethod = AuthenticationMethod.OPENID;

	@Column(name = "is_legacy", nullable = false, columnDefinition = "char(1) default 'N'")
	@Type(type = "yes_no")
	private boolean legacy = false;

	@Column(name = "legacy_marketplace_base_url")
	private String legacyMarketplaceBaseUrl;
}
