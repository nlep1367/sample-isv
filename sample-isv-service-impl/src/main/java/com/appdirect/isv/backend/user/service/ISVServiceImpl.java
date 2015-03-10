package com.appdirect.isv.backend.user.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.backend.core.dao.GenericDAO;
import com.appdirect.isv.backend.user.model.Account;
import com.appdirect.isv.backend.user.model.Addon;
import com.appdirect.isv.backend.user.model.User;
import com.appdirect.isv.backend.user.vo.AccountBean;
import com.appdirect.isv.backend.user.vo.AddonBean;
import com.appdirect.isv.backend.user.vo.UserBean;

@Service("isvService")
public class ISVServiceImpl implements ISVService {
	@Autowired
	private GenericDAO<Account, Long> accountDao;
	@Autowired
	private GenericDAO<User, Long> userDao;
	@Autowired
	private GenericDAO<Addon, Long> addonDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long createAccount(AccountBean accountBean, UserBean adminBean) {
		User admin = adminBean.toUser();
		admin.setAdmin(true);
		Account account = accountBean.toAccount();
		admin.setAccount(account);
		account.getUsers().add(admin);
		accountDao.saveOrUpdate(account);
		return account.getId();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void createUser(UserBean userBean, AccountBean accountBean) {
		Account account = readAccount(accountBean);
		User user = userBean.toUser();
		user.setAccount(account);
		account.getUsers().add(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserBean readUserByOpenID(String openId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class)
				.add(Restrictions.eq("openId", openId));
		User user = userDao.findUniqueByCriteria(User.class, criteria);
		if (user == null) {
			return null;
		}
		return new UserBean(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<UserBean> readUsers() {
		List<UserBean> userBeans = new ArrayList<UserBean>();
		List<User> users = userDao.findAll();
		for (User user : users) {
			userBeans.add(new UserBean(user));
		}
		return userBeans;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public AccountBean readAccountByUUID(String accountUuid) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Account.class)
			.add(Restrictions.eq("uuid", accountUuid));
		Account account = accountDao.findUniqueByCriteria(Account.class, criteria);
		if (account == null) {
			return null;
		}
		return new AccountBean(account);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public AccountBean readAccountByID(Long accountId) {
		try {
			return new AccountBean(accountDao.findById(accountId));
		} catch (ObjectNotFoundException e) {
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public AccountBean readAccountByUserID(Long userId) {
		try {
			User user = userDao.findById(userId);
			return new AccountBean(user.getAccount());
		} catch (ObjectNotFoundException e) {
			return null;
		}
	}

	private Account readAccount(AccountBean accountBean) {
		Account account = null;
		if (account == null && accountBean.getId() != null) {
			DetachedCriteria criteria = DetachedCriteria.forClass(Account.class)
				.add(Restrictions.eq("id", accountBean.getId()));
			account = accountDao.findUniqueByCriteria(Account.class, criteria);
		}
		if (account == null && accountBean.getUuid() != null) {
			DetachedCriteria criteria = DetachedCriteria.forClass(Account.class)
				.add(Restrictions.eq("uuid", accountBean.getUuid()));
			account = accountDao.findUniqueByCriteria(Account.class, criteria);
		}
		if (account == null) {
			throw new ObjectNotFoundException(accountBean.toString(), Account.class.toString());
		}
		return account;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<AccountBean> readAccounts() {
		DetachedCriteria criteria = DetachedCriteria.forClass(Account.class)
				.addOrder(Order.desc("id"));
		List<Account> accounts = accountDao.findByCriteria(Account.class, criteria, 0, 25);
		List<AccountBean> accountBeans = new ArrayList<AccountBean>(accounts.size());
		for (Account account : accounts) {
			accountBeans.add(new AccountBean(account));
		}
		return accountBeans;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(UserBean userBean) {
		User user = userDao.findById(userBean.getId());
		user.setEmail(userBean.getEmail());
		user.setFirstName(userBean.getFirstName());
		user.setLastName(userBean.getLastName());
		user.setZipCode(userBean.getZipCode());
		user.setDepartment(userBean.getDepartment());
		user.setTimezone(userBean.getTimezone());
		user.setAdmin(userBean.isAdmin());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(AccountBean accountBean) {
		Account account = readAccount(accountBean);
		account.setName(accountBean.getName());
		account.setEditionCode(accountBean.getEditionCode());
		account.setMaxUsers(accountBean.getMaxUsers());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(AccountBean accountBean) {
		Account account = readAccount(accountBean);
		accountDao.delete(account);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteUser(Long userId) {
		User user = userDao.findById(userId);
		user.getAccount().getUsers().remove(user);
		user.setAccount(null);
		userDao.delete(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void createAddon(AddonBean addonBean, AccountBean accountBean) {
		Account account = readAccount(accountBean);
		Addon addon = addonBean.toAddon();
		addon.setAccount(account);
		account.getAddons().add(addon);
	}

	private Addon readAddon(AddonBean addonBean) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Addon.class)
				.add(Restrictions.eq("addonIdentifier", addonBean.getAddonIdentifier()));
		Addon addon = addonDao.findUniqueByCriteria(Addon.class, criteria);
		if (addon == null) {
			throw new ObjectNotFoundException(addonBean.toString(), Addon.class.toString());
		}
		return addon;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateAddon(AddonBean addonBean) {
		Addon addon = readAddon(addonBean);
		addon.setCode(addonBean.getCode());
		addon.setQuantity(addonBean.getQuantity());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAddon(AddonBean addonBean) {
		Addon addon = readAddon(addonBean);
		Account account = addon.getAccount();
		account.getAddons().remove(addon);
		addon.setAccount(null);
		addonDao.delete(addon);
	}
}
