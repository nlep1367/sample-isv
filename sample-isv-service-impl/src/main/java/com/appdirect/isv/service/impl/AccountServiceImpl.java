package com.appdirect.isv.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.AddonBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.model.Account;
import com.appdirect.isv.model.Addon;
import com.appdirect.isv.model.User;
import com.appdirect.isv.repository.AccountRepository;
import com.appdirect.isv.repository.AddonRepository;
import com.appdirect.isv.repository.UserRepository;
import com.appdirect.isv.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AddonRepository addonRepository;

	private Account readAccountById(Long accountId) {
		Account account = accountRepository.findOne(accountId);
		if (account == null) {
			throw new ObjectNotFoundException(accountId, Account.class.toString());
		}
		return account;
	}

	private User readUserById(Long userId) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new ObjectNotFoundException(userId, Account.class.toString());
		}
		return user;
	}

	private Addon readAddonById(Long addonId) {
		Addon addon = addonRepository.findOne(addonId);
		if (addon == null) {
			throw new ObjectNotFoundException(addonId, Addon.class.toString());
		}
		return addon;
	}

	/*
	 * Account operations.
	 */

	@Override
	@Transactional
	public void createAccount(AccountBean accountBean, UserBean adminBean) {
		User admin = adminBean.toUser();
		userRepository.save(admin);
		adminBean.setId(admin.getId());

		Account account = accountBean.toAccount();
		account.addUser(admin);
		accountRepository.save(account);
		accountBean.setId(account.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<AccountBean> readAccounts() {
		Pageable pageable = new PageRequest(0, 25, Direction.DESC, "id");
		Page<Account> page = accountRepository.findAll(pageable);
		return page.getContent().stream()
				.map(account -> new AccountBean(account))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public AccountBean readAccount(Long accountId) {
		Account account = readAccountById(accountId);
		return new AccountBean(account);
	}

	@Override
	@Transactional
	public void updateAccount(AccountBean accountBean) {
		Account account = readAccountById(accountBean.getId());
		account.setEditionCode(accountBean.getEditionCode());
		account.setMaxUsers(accountBean.getMaxUsers());
	}

	@Override
	@Transactional
	public void deleteAccount(Long accountId) {
		Account account = readAccountById(accountId);
		accountRepository.delete(account);
	}

	/*
	 * User operations.
	 */

	@Override
	@Transactional
	public void createUser(UserBean userBean, Long accountId) {
		Account account = readAccountById(accountId);
		User user = userBean.toUser();
		account.addUser(user);
		userRepository.save(user);
		userBean.setId(user.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBean> readUsers() {
		Pageable pageable = new PageRequest(0, 25, Direction.DESC, "id");
		Page<User> page = userRepository.findAll(pageable);
		return page.getContent().stream()
				.map(user -> new UserBean(user))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public AccountBean readUserAccount(Long userId) {
		User user = readUserById(userId);
		return new AccountBean(user.getAccount());
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		User user = readUserById(userId);
		user.getAccount().getUsers().remove(user);
		user.setAccount(null);
		userRepository.delete(user);
	}

	/*
	 * Addon operations.
	 */

	@Override
	@Transactional
	public void createAddon(AddonBean addonBean, Long accountId) {
		Account account = readAccountById(accountId);
		Addon addon = addonBean.toAddon();
		account.addAddon(addon);
		addonRepository.save(addon);
		addonBean.setId(addon.getId());
	}

	@Override
	@Transactional
	public void updateAddon(AddonBean addonBean) {
		Addon addon = readAddonById(addonBean.getId());
		addon.setCode(addonBean.getCode());
		addon.setQuantity(addonBean.getQuantity());
	}

	@Override
	@Transactional
	public void deleteAddon(Long addonId) {
		Addon addon = readAddonById(addonId);
		Account account = addon.getAccount();
		account.getAddons().remove(addon);
		addon.setAccount(null);
		addonRepository.delete(addon);
	}
}
