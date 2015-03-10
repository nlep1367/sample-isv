package com.appdirect.isv.integration.remote.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement(name = "result")
@Data @NoArgsConstructor
public class APIResult implements Serializable {
	private static final long serialVersionUID = -7599199539526987847L;

	private boolean success;
	private boolean asynchronous = false;
	private ErrorCode errorCode;
	private String message;
	private String accountIdentifier;
	private String userIdentifier;
	private String id;

	public APIResult(boolean success, String message) {
		this(success, null, message);
	}

	public APIResult(boolean success, ErrorCode errorCode, String message) {
		super();
		this.success = success;
		this.errorCode = errorCode;
		this.message = message;
	}

	@XmlTransient
	@JsonIgnore
	public boolean isAsynchronous() {
		return asynchronous;
	}
}
