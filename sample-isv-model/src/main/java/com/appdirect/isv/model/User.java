package com.appdirect.isv.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Type;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "isv_users")
public class User extends TimestampedObject {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "uuid")
	private String appDirectUuid;

	@Column(name = "open_id")
	private String appDirectOpenId;

	@Column(name = "email")
	private String email;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "zip_code")
	private String zipCode;

	@Column(name = "timezone")
	private String timezone;

	@Column(name = "department")
	private String department;

	@Column(name = "is_admin", nullable = false, columnDefinition = "char(1) default 'N'")
	@Type(type = "yes_no")
	private boolean admin = false;

	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;
}
