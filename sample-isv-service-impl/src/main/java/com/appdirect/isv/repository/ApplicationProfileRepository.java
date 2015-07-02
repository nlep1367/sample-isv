package com.appdirect.isv.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.appdirect.isv.model.ApplicationProfile;

public interface ApplicationProfileRepository extends PagingAndSortingRepository<ApplicationProfile, String> {
	ApplicationProfile findByOauthConsumerKey(String oauthConsumerKey);
}
