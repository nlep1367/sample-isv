package com.appdirect.isv.backend.user.vo;

import java.io.Serializable;

import lombok.Data;

import com.appdirect.isv.backend.user.model.Addon;

@Data
public class AddonBean implements Serializable {
	private static final long serialVersionUID = 8804813209638282820L;

	private Long id;
	private String addonIdentifier;
	private String code;
	private Integer quantity;

	public AddonBean() {
		super();
	}

	public AddonBean(Addon addon) {
		setId(addon.getId());
		setAddonIdentifier(addon.getAddonIdentifier());
		setCode(addon.getCode());
		setQuantity(addon.getQuantity());
	}
}
