package com.appdirect.isv.backend.integration.remote.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "account")
public class AccountInfo implements Serializable {
	private static final long serialVersionUID = -400499571158068365L;

	private String accountIdentifier;
	private String status;

	public AccountInfo() {
		super();
	}
}
