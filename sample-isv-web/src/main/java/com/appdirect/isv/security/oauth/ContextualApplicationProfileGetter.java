package com.appdirect.isv.security.oauth;

import java.util.Optional;

import com.appdirect.isv.model.ApplicationProfile;

public interface ContextualApplicationProfileGetter {
	Optional<ApplicationProfile> get();
}
