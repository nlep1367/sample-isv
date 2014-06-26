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

import lombok.Data;

import com.appdirect.isv.backend.user.vo.AddonBean;

@Data
@Entity
@Table(name = "isv_addons")
public class Addon implements Serializable {
	private static final long serialVersionUID = 560036415271867806L;

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

	public Addon() {
		super();
	}

	public Addon(AddonBean addonBean) {
		super();
		populate(addonBean);
	}

	public void populate(AddonBean addonBean) {
		setAddonIdentifier(addonBean.getAddonIdentifier());
		setCode(addonBean.getCode());
		setQuantity(addonBean.getQuantity());
	}
}
