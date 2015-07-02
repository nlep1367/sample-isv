package com.appdirect.isv.security.oauth;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth.provider.ConsumerAuthentication;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.stereotype.Component;

import com.appdirect.isv.model.ApplicationProfile;

@Slf4j
@Component
public class ContextualApplicationProfileGetterImpl implements ContextualApplicationProfileGetter {
	@Autowired
	private SecurityContextHolderStrategy securityContextHolderStrategy;

	@Override
	public Optional<ApplicationProfile> get() {
		Authentication authentication = securityContextHolderStrategy.getContext().getAuthentication();
		if (!(authentication instanceof ConsumerAuthentication)) {
			log.warn("authentication instanceof " + authentication.getClass().getCanonicalName());
			return Optional.empty();
		}
		ConsumerDetails consumerDetails = ((ConsumerAuthentication) authentication).getConsumerDetails();
		if (!(consumerDetails instanceof AppDirectConsumerDetails)) {
			log.warn("consumerDetails is an instance of " + consumerDetails.getClass().getCanonicalName());
			return Optional.empty();
		}
		return Optional.ofNullable(((AppDirectConsumerDetails) consumerDetails).getApplicationProfile());
	}
}
