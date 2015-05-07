package com.appdirect.isv.integration.remote.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * Extend this class if a resource (WS object) embeds other resources.
 */
@Getter
public abstract class HalResourceSupport extends ResourceSupport {
	@XmlTransient
	@JsonInclude(Include.NON_EMPTY)
	@JsonProperty("_embedded")
	private final Map<String, Object> embedded = new HashMap<>();

	@Override
	@XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
	@JsonProperty("links")
	@JsonInclude(Include.NON_EMPTY)
	public List<Link> getLinks() {
		return super.getLinks();
	}

	public HalResourceSupport embed(String relationship, Object resource) {
		Preconditions.checkArgument(StringUtils.isNotBlank(relationship));
		embedded.put(relationship, resource);
		return this;
	}
}
