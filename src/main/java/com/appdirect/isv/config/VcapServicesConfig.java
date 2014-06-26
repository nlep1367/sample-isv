package com.appdirect.isv.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component("vcapServicesConfig")
public class VcapServicesConfig {
	private static final String VCAP_SERVICES = "VCAP_SERVICES";

	@Getter private final String mySqlUri;
	@Getter private final String mySqlUsername;
	@Getter private final String mySqlPassword;

	public VcapServicesConfig() throws JsonProcessingException, IOException {
		String mySqlUri = "jdbc:mysql://localhost:3306/isv?useUnicode=true&characterEncoding=UTF-8";
		String mySqlUsername = "root";
		String mySqlPassword = "password";
		String vcapServicesEnvStr = System.getenv(VCAP_SERVICES);
		if (StringUtils.isNotBlank(vcapServicesEnvStr)) {
			JsonNode vcapServicesJson = (new ObjectMapper()).readTree(vcapServicesEnvStr);
			Iterator<Entry<String, JsonNode>> iterator = vcapServicesJson.fields();
			while (iterator.hasNext()) {
				Entry<String, JsonNode> entry = iterator.next();
				if (entry.getKey().startsWith("cleardb") && entry.getValue() instanceof ArrayNode) {
					JsonNode credentials = entry.getValue().get(0).get("credentials");
					mySqlUri = "jdbc:" + credentials.get("uri").textValue();
					mySqlUsername = credentials.get("username").textValue();
					mySqlPassword = credentials.get("password").textValue();
					break;
				}
			}
		}
		this.mySqlUri = mySqlUri;
		this.mySqlUsername = mySqlUsername;
		this.mySqlPassword = mySqlPassword;
	}
}
