package com.appdirect.isv.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.appdirect.isv.model.Account;

public interface AccountRepository extends PagingAndSortingRepository<Account, Long> {
	Account findBySamlIdpEntityId(String samlIdpEntityId);
}
