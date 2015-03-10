package com.appdirect.isv.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.appdirect.isv.model.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
	Account findByUuid(String uuid);

	Page<Account> findAll(Pageable pageable);
}
