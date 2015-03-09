package com.appdirect.isv.backend.integration.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.wicket.util.time.Time;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.backend.core.dao.GenericDAO;
import com.appdirect.isv.backend.core.util.ServerConfiguration;
import com.appdirect.isv.backend.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.backend.integration.remote.type.ErrorCode;
import com.appdirect.isv.backend.integration.remote.type.EventType;
import com.appdirect.isv.backend.integration.remote.vo.APIResult;
import com.appdirect.isv.backend.integration.remote.vo.EventInfo;
import com.appdirect.isv.backend.integration.remote.vo.OrderInfo;
import com.appdirect.isv.backend.integration.util.IntegrationUtils;
import com.appdirect.isv.backend.user.model.Account;
import com.appdirect.isv.backend.user.model.User;
import com.appdirect.isv.backend.user.service.ISVService;
import com.appdirect.isv.backend.user.vo.AccountBean;
import com.appdirect.isv.backend.user.vo.AddonBean;
import com.appdirect.isv.backend.user.vo.UserBean;

@Component
public class IntegrationHelper {
	private static final String ZIP_CODE_KEY = "zipCode";
	private static final String DEPARTMENT_KEY = "department";
	private static final String TIMEZONE_KEY = "timezone";
	private static final String APP_ADMIN = "appAdmin";

	@Autowired
	private GenericDAO<User, Long> userDao;
	@Autowired
	private ServerConfiguration serverConfiguration;
	@Autowired
	private ISVService isvService;

	@Transactional(propagation = Propagation.REQUIRED)
	public APIResult processEvent(String eventUrl, String token) {
		APIResult result = new APIResult();
		result.setSuccess(true);
		String basePath = serverConfiguration.getAppDirectBaseUrl();
		if (StringUtils.isNotBlank(eventUrl)) {
			basePath = IntegrationUtils.extractBasePath(eventUrl);
			token = IntegrationUtils.extractToken(eventUrl);
		}
		AppDirectIntegrationAPI api = JAXRSClientFactory.create(basePath, AppDirectIntegrationAPI.class);
		final EventInfo eventInfo = api.readEvent(token);
		if (eventInfo == null || eventInfo.getType() == null) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.UNKNOWN_ERROR);
			result.setMessage("Event info not found or invalid.");
			return result;
		}
		if (StringUtils.isNotBlank(eventUrl) && !basePath.equals(eventInfo.getMarketplace().getBaseUrl())) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.UNKNOWN_ERROR);
			result.setMessage("Event partner mismatch.");
			return result;
		}
		switch(eventInfo.getType()) {
			case SUBSCRIPTION_ORDER:
				processSubscriptionOrderEvent(basePath, eventInfo, result);
				break;
			case SUBSCRIPTION_CHANGE:
				processSubscriptionChangeEvent(basePath, eventInfo, result);
				break;
			case SUBSCRIPTION_CANCEL:
				processSubscriptionCancelEvent(eventInfo, result);
				break;
			case USER_ASSIGNMENT:
				processUserAssignmentEvent(eventInfo, result);
				break;
			case USER_UNASSIGNMENT:
				processUserUnassignmentEvent(eventInfo, result);
				break;
			case SUBSCRIPTION_NOTICE:
				break;
			case ADDON_ORDER:
				processAddonOrderEvent(eventInfo, result);
				break;
			case ADDON_CHANGE:
				processAddonChangeEvent(eventInfo, result);
				break;
			case ADDON_CANCEL:
				processAddonCancelEvent(eventInfo, result);
				break;
			default:
				result.setSuccess(false);
				result.setErrorCode(ErrorCode.UNKNOWN_ERROR);
				result.setMessage("Event type not supported by this endpoint: " + String.valueOf(eventInfo.getType()));
				break;
		}
		return result;
	}

	private void processSubscriptionOrderEvent(String appDirectBaseUrl, EventInfo eventInfo, APIResult result) {
		// Create the account.
		UserBean adminBean = new UserBean();
		adminBean.setUuid(eventInfo.getCreator().getUuid());
		adminBean.setOpenId(eventInfo.getCreator().getOpenId());
		adminBean.setEmail(eventInfo.getCreator().getEmail());
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class)
				.add(Restrictions.eq("openId", adminBean.getOpenId()));
		List<User> existingUsers = userDao.findByCriteria(User.class, criteria);
		User existingUser = existingUsers == null || existingUsers.isEmpty() ? null : existingUsers.get(0);
		if (existingUser != null) {
			Account existingAccount = existingUser.getAccount();
			if (existingAccount.isAppDirectManaged()) {
				// We already have an AppDirect account mapped for this user. Reconcile.
				existingAccount.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
				existingAccount.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
				result.setSuccess(true);
				result.setErrorCode(null);
				result.setAccountIdentifier(existingAccount.getUuid());
				result.setMessage("An account with this user already exists but is correctly mapped.");
				return;
			}
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.USER_ALREADY_EXISTS);
			result.setMessage("An account with this user already exists.");
			return;
		} else {
			adminBean.setFirstName(eventInfo.getCreator().getFirstName());
			adminBean.setLastName(eventInfo.getCreator().getLastName());
			adminBean.setAdmin(true);
			AccountBean accountBean = new AccountBean();
			accountBean.setUuid(eventInfo.getPayload().getCompany().getUuid());
			accountBean.setName(accountBean.getUuid());
			accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
			accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
			accountBean.setAppDirectManaged(true);
			accountBean.setAppDirectBaseUrl(appDirectBaseUrl);
			isvService.createAccount(accountBean, adminBean);
			result.setSuccess(true);
			result.setAccountIdentifier(accountBean.getUuid());
		}
	}

	private void processSubscriptionChangeEvent(String appDirectBaseUrl, EventInfo eventInfo, APIResult result) {
		try {
			AccountBean accountBean = new AccountBean();
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
			accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
			accountBean.setAppDirectManaged(true);
			accountBean.setAppDirectBaseUrl(appDirectBaseUrl);
			isvService.update(accountBean);
			result.setSuccess(true);
			result.setMessage(String.format("Successfully updated account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		} catch (ObjectNotFoundException onfe) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.ACCOUNT_NOT_FOUND);
			result.setMessage(String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
	}

	private void processSubscriptionCancelEvent(EventInfo eventInfo, APIResult result) {
		try {
			AccountBean accountBean = new AccountBean();
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			isvService.delete(accountBean);
			result.setSuccess(true);
			result.setMessage(String.format("Successfully deleted account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		} catch (ObjectNotFoundException e) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.ACCOUNT_NOT_FOUND);
			result.setMessage(String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
	}

	private void processUserAssignmentEvent(EventInfo eventInfo, APIResult result) {
		if (eventInfo.getType() != EventType.USER_ASSIGNMENT) {
			throw new RuntimeException("eventInfo not of the right type.");
		}
		AccountBean accountBean = new AccountBean();
		accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
		// Read info about the user.
		UserBean userBean = new UserBean();
		userBean.setUuid(eventInfo.getPayload().getUser().getUuid());
		userBean.setOpenId(eventInfo.getPayload().getUser().getOpenId());
		userBean.setEmail(eventInfo.getPayload().getUser().getEmail());
		userBean.setFirstName(eventInfo.getPayload().getUser().getFirstName());
		userBean.setLastName(eventInfo.getPayload().getUser().getLastName());
		boolean admin = false;
		if (eventInfo.getPayload().getUser().getAttributes() != null) {
			userBean.setZipCode(eventInfo.getPayload().getUser().getAttributes().get(ZIP_CODE_KEY));
			userBean.setDepartment(eventInfo.getPayload().getUser().getAttributes().get(DEPARTMENT_KEY));
			userBean.setTimezone(eventInfo.getPayload().getUser().getAttributes().get(TIMEZONE_KEY));
			admin = Boolean.parseBoolean(eventInfo.getPayload().getUser().getAttributes().get(APP_ADMIN));
		}
		userBean.setAdmin(admin);
		// AppDirect is trying to create a new user.
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class).add(Restrictions.eq("openId", userBean.getOpenId()));
		List<User> existingUsers = userDao.findByCriteria(User.class, criteria);
		User existingUser = existingUsers == null || existingUsers.isEmpty() ? null : existingUsers.get(0);
		if (existingUser != null) {
			if (StringUtils.equals(existingUser.getAccount().getUuid(), accountBean.getUuid())) {
				result.setSuccess(true);
				result.setErrorCode(null);
				result.setMessage("A user with this OpenID already exists but was mapped correctly.");
				return;
			} else {
				// A user with the same OpenID already exists in a different account.
				// Fail.
				result.setSuccess(false);
				result.setErrorCode(ErrorCode.USER_ALREADY_EXISTS);
				result.setMessage("A user with this OpenID or email already exists.");
			}
		} else {
			try {
				// Create the new user.
				isvService.createUser(userBean, accountBean);
				result.setMessage("Successfully created user: " + userBean.getUsername());
			} catch (ObjectNotFoundException onfe) {
				// The account could not be found. Fail.
				result.setSuccess(false);
				result.setErrorCode(ErrorCode.ACCOUNT_NOT_FOUND);
				result.setMessage(onfe.getMessage());
			}
		}
	}

	private void processUserUnassignmentEvent(EventInfo eventInfo, APIResult result) {
		if (eventInfo.getType() != EventType.USER_UNASSIGNMENT) {
			throw new RuntimeException("eventInfo not of the right type.");
		}
		try {
			AccountBean accountBean = new AccountBean();
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			User user = readUserByOpenID(eventInfo.getPayload().getUser().getOpenId());
			if (!StringUtils.equals(accountBean.getUuid(), user.getAccount().getUuid())) {
				// The user account is not the same as the account passed
				// in. We can't allow that. Fail.
				result.setSuccess(false);
				result.setErrorCode(ErrorCode.UNAUTHORIZED);
				result.setMessage("User does not belong to the expected account.");
			} else {
				user.getAccount().getUsers().remove(user);
				user.setAccount(null);
				this.userDao.delete(user);
				result.setMessage("Successfully deleted user: " + eventInfo.getPayload().getUser().getOpenId());
			}
		} catch (ObjectNotFoundException onfe) {
			// The user could not be found. Fail.
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.USER_NOT_FOUND);
			result.setMessage(onfe.getMessage());
		}
	}

	private User readUserByOpenID(String openId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class)
			.add(Restrictions.eq("openId", openId));
		List<User> users = this.userDao.findByCriteria(User.class, criteria);
		if (users != null && users.size() == 1) {
			return users.get(0);
		}
		throw new ObjectNotFoundException(openId, User.class.toString());
	}

	private void processAddonOrderEvent(EventInfo eventInfo, APIResult result) {
		if (eventInfo.getType() != EventType.ADDON_ORDER) {
			throw new RuntimeException("eventInfo not of the right type.");
		}
		try {
			AccountBean accountBean = new AccountBean();
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			AddonBean addonBean = new AddonBean();
			OrderInfo orderInfo = eventInfo.getPayload().getOrder();
			addonBean.setCode(orderInfo.getAddonOfferingCode());
			addonBean.setAddonIdentifier(String.format("%s-%s-%s", accountBean.getUuid(), addonBean.getCode(), Time.now()));
			if (!orderInfo.getItems().isEmpty()) {
				addonBean.setQuantity(orderInfo.getItems().get(0).getQuantity());
			}
			isvService.createAddon(addonBean, accountBean);
			result.setSuccess(true);
			result.setId(addonBean.getAddonIdentifier());
		} catch (ObjectNotFoundException onfe) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.ACCOUNT_NOT_FOUND);
			result.setMessage(onfe.getMessage());
		}
	}

	private void processAddonChangeEvent(EventInfo eventInfo, APIResult result) {
		if (eventInfo.getType() != EventType.ADDON_CHANGE) {
			throw new RuntimeException("eventInfo not of the right type.");
		}
		try {
			AccountBean accountBean = new AccountBean();
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			AddonBean addonBean = new AddonBean();
			OrderInfo orderInfo = eventInfo.getPayload().getOrder();
			addonBean.setCode(orderInfo.getAddonOfferingCode());
			addonBean.setAddonIdentifier(eventInfo.getPayload().getAddonInstance().getId());
			addonBean.setQuantity(orderInfo.getItems().get(0).getQuantity());
			isvService.updateAddon(addonBean);
			result.setSuccess(true);
		} catch (ObjectNotFoundException onfe) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.UNKNOWN_ERROR);
			result.setMessage(onfe.getMessage());
		}
	}

	private void processAddonCancelEvent(EventInfo eventInfo, APIResult result) {
		if (eventInfo.getType() != EventType.ADDON_CANCEL) {
			throw new RuntimeException("eventInfo not of the right type.");
		}
		try {
			AddonBean addonBean = new AddonBean();
			addonBean.setAddonIdentifier(eventInfo.getPayload().getAddonInstance().getId());
			isvService.deleteAddon(addonBean);
			result.setSuccess(true);
			result.setMessage(String.format("Successfully cancel addon: %s", eventInfo.getPayload().getAddonInstance().getId()));
		} catch (ObjectNotFoundException onfe) {
			result.setSuccess(false);
			result.setErrorCode(ErrorCode.ACCOUNT_NOT_FOUND);
			result.setMessage(onfe.getMessage());
		}
	}
}
