package com.appdirect.isv.repository;

import org.springframework.data.repository.CrudRepository;

import com.appdirect.isv.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
	User findByOpenId(String openId);
}
