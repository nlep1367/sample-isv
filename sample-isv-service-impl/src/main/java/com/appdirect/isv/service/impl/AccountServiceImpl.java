package com.appdirect.isv.service.impl;

import java.util.ArrayList;
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

	@Override
	@Transactional
	public Long createAccount(AccountBean accountBean, UserBean adminBean) {
		User admin = adminBean.toUser();
		admin.setAdmin(true);
		Account account = accountBean.toAccount();
		admin.setAccount(account);
		account.getUsers().add(admin);
		accountRepository.save(account);
		return account.getId();
	}

	@Override
	@Transactional
	public void createUser(UserBean userBean, AccountBean accountBean) {
		Account account = readAccount(accountBean);
		User user = userBean.toUser();
		user.setAccount(account);
		account.getUsers().add(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserBean readUserByOpenID(String openId) {
		User user = userRepository.findByOpenId(openId);
		if (user == null) {
			return null;
		}
		return new UserBean(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBean> readUsers() {
		List<UserBean> userBeans = new ArrayList<UserBean>();
		Iterable<User> users = userRepository.findAll();
		for (User user : users) {
			userBeans.add(new UserBean(user));
		}
		return userBeans;
	}

	@Override
	@Transactional(readOnly = true)
	public AccountBean readAccountByUUID(String accountUuid) {
		Account account = accountRepository.findByUuid(accountUuid);
		if (account == null) {
			return null;
		}
		return new AccountBean(account);
	}

	@Override
	@Transactional(readOnly = true)
	public AccountBean readAccountByID(Long accountId) {
		try {
			return new AccountBean(accountRepository.findOne(accountId));
		} catch (ObjectNotFoundException e) {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AccountBean readAccountByUserID(Long userId) {
		try {
			User user = userRepository.findOne(userId);
			return new AccountBean(user.getAccount());
		} catch (ObjectNotFoundException e) {
			return null;
		}
	}

	private Account readAccount(AccountBean accountBean) {
		Account account = null;
		if (account == null && accountBean.getId() != null) {
			account = accountRepository.findOne(accountBean.getId());
		}
		if (account == null && accountBean.getUuid() != null) {
			account = accountRepository.findByUuid(accountBean.getUuid());
		}
		if (account == null) {
			throw new ObjectNotFoundException(accountBean.toString(), Account.class.toString());
		}
		return account;
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
	@Transactional
	public void update(AccountBean accountBean) {
		Account account = readAccount(accountBean);
		account.setEditionCode(accountBean.getEditionCode());
		account.setMaxUsers(accountBean.getMaxUsers());
	}

	@Override
	@Transactional
	public void delete(AccountBean accountBean) {
		Account account = readAccount(accountBean);
		accountRepository.delete(account);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		User user = userRepository.findOne(userId);
		user.getAccount().getUsers().remove(user);
		user.setAccount(null);
		userRepository.delete(user);
	}

	@Override
	@Transactional
	public void createAddon(AddonBean addonBean, AccountBean accountBean) {
		Account account = readAccount(accountBean);
		Addon addon = addonBean.toAddon();
		addon.setAccount(account);
		account.getAddons().add(addon);
	}

	private Addon readAddon(AddonBean addonBean) {
		Addon addon = addonRepository.findByAddonIdentifier(addonBean.getAddonIdentifier());
		if (addon == null) {
			throw new ObjectNotFoundException(addonBean.toString(), Addon.class.toString());
		}
		return addon;
	}

	@Override
	@Transactional
	public void updateAddon(AddonBean addonBean) {
		Addon addon = readAddon(addonBean);
		addon.setCode(addonBean.getCode());
		addon.setQuantity(addonBean.getQuantity());
	}

	@Override
	@Transactional
	public void deleteAddon(AddonBean addonBean) {
		Addon addon = readAddon(addonBean);
		Account account = addon.getAccount();
		account.getAddons().remove(addon);
		addon.setAccount(null);
		addonRepository.delete(addon);
	}
}
