package com.appdirect.isv.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.appdirect.isv.model.Addon;

@Data @NoArgsConstructor
public class AddonBean implements Serializable {
	private static final long serialVersionUID = 8804813209638282820L;

	private Long id;
	private String addonIdentifier;
	private String code;
	private Integer quantity;

	public AddonBean(Addon addon) {
		this.id = addon.getId();
		this.addonIdentifier = addon.getAddonIdentifier();
		this.code = addon.getCode();
		this.quantity = addon.getQuantity();
	}

	public Addon toAddon() {
		Addon addon = new Addon();
		addon.setAddonIdentifier(getAddonIdentifier());
		addon.setCode(getCode());
		addon.setQuantity(getQuantity());
		return addon;
	}
}
