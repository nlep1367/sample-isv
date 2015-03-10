package com.appdirect.isv.integration.remote.vo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@XmlRootElement(name = "addonBinding")
public class AddonBindingInfo implements Serializable {
	private static final long serialVersionUID = 3688351528247437199L;

	private String id;
}
