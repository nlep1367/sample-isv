package com.appdirect.isv.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.appdirect.isv.model.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {
	User findByOpenId(String openId);

	User findByUuid(String uuid);
}
