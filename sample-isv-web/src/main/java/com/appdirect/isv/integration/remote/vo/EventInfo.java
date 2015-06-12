package com.appdirect.isv.integration.remote.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.appdirect.isv.integration.remote.type.EventType;

@Getter @Setter @NoArgsConstructor
@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventInfo extends HalResourceSupport implements Serializable {
	private static final long serialVersionUID = 2658400228024450854L;

	private EventType type;
	private MarketplaceInfo marketplace;
	private String applicationUuid;
	private EventFlag flag;
	private UserInfo creator;
	private EventPayload payload;
	private String returnUrl;
}
