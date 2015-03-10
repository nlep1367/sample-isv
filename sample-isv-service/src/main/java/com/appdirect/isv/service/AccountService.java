package com.appdirect.isv.service;

import java.util.List;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.AddonBean;
import com.appdirect.isv.dto.UserBean;

public interface AccountService {
	Long createAccount(AccountBean accountBean, UserBean adminBean);

	void createUser(UserBean userBean, AccountBean accountBean);

	UserBean readUserByOpenID(String openId);

	List<UserBean> readUsers();

	AccountBean readAccountByUUID(String accountUuid);

	AccountBean readAccountByID(Long accountId);

	AccountBean readAccountByUserID(Long userId);

	List<AccountBean> readAccounts();

	void update(AccountBean accountBean);

	void delete(AccountBean accountBean);

	void deleteUser(Long userId);

	void createAddon(AddonBean addonBean, AccountBean accountBean);

	void updateAddon(AddonBean addonBean);

	void deleteAddon(AddonBean addonBean);
}
