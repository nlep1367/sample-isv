package com.appdirect.isv.service;

import java.util.List;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.AddonBean;
import com.appdirect.isv.dto.UserBean;

public interface AccountService {
	/*
	 * Account operations.
	 */

	void createAccount(AccountBean accountBean, UserBean adminBean);

	List<AccountBean> readAccounts();

	AccountBean readAccount(Long accountId);

	void updateAccount(AccountBean accountBean);

	void deleteAccount(Long accountId);

	/*
	 * User operations.
	 */

	void createUser(UserBean userBean, Long accountId);

	List<UserBean> readUsers();

	AccountBean readUserAccount(Long userId);

	void deleteUser(Long userId);

	/*
	 * Addon operations.
	 */

	void createAddon(AddonBean addonBean, Long accountId);

	void updateAddon(AddonBean addonBean);

	void deleteAddon(Long addonId);
}
