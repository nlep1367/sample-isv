package com.appdirect.isv.backend.user.model;

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

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "isv_addons")
public class Addon {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "addon_identifier", unique = true)
	private String addonIdentifier;

	@Column(name = "code")
	private String code;

	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;

	@Column(name = "quantity")
	private Integer quantity;
}
