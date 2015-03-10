package com.appdirect.isv.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.service.AccountService;

@RestController
@RequestMapping(value = "/api/service", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
	@Autowired
	private AccountService accountService;

	@RequestMapping("/users")
	public Resources<UserBean> readUsers() {
		return new Resources<>(accountService.readUsers());
	}

	@RequestMapping("/accounts/{accountUuid}")
	public AccountBean readAccountByUUID(@PathVariable("accountUuid") String accountUuid) {
		return accountService.readAccountByUUID(accountUuid);
	}

	@RequestMapping("/accounts")
	public Resources<AccountBean> readAccounts() {
		return new Resources<>(accountService.readAccounts());
	}
}
