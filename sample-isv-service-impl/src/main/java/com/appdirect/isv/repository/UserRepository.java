package com.appdirect.isv.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.appdirect.isv.model.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {
	List<User> findByAppDirectOpenId(String appDirectOpenId);

	List<User> findByAppDirectUuid(String appDirectUuid);
}
