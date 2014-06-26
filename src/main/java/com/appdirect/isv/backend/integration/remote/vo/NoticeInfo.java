package com.appdirect.isv.backend.integration.remote.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.appdirect.isv.backend.integration.remote.type.NoticeType;

/**
 * The XML payload that is returned for Subscription Notice events
 *
 * @author Steve Weis (steve.weis@appdirect.com)
 */
@XmlRootElement(name = "notice")
public class NoticeInfo implements Serializable {
	private static final long serialVersionUID = 482426190291639798L;

	private NoticeType type;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public NoticeType getType() {
		return type;
	}

	public void setType(NoticeType type) {
		this.type = type;
	}
}
