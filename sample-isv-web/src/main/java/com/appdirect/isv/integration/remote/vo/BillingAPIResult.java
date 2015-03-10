package com.appdirect.isv.integration.remote.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@XmlRootElement(name = "result")
public class BillingAPIResult implements Serializable {
	private static final long serialVersionUID = -7027507409588330850L;

	private boolean success;
	private String message;
}
