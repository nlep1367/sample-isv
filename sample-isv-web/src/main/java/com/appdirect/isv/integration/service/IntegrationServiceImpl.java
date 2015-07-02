package com.appdirect.isv.integration.service;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.wicket.util.time.Time;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.AddonBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.integration.oauth.OAuthPhaseInterceptor;
import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.integration.oauth.OAuthUrlSignerImpl;
import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.type.EventType;
import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.integration.remote.vo.OrderInfo;
import com.appdirect.isv.integration.remote.vo.saml.SamlRelyingPartyWS;
import com.appdirect.isv.integration.util.IntegrationUtils;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.model.User;
import com.appdirect.isv.repository.UserRepository;
import com.appdirect.isv.service.AccountService;
import com.google.common.base.Preconditions;

@Service
public class IntegrationServiceImpl implements IntegrationService {
	private static final String ZIP_CODE_KEY = "zipCode";
	private static final String DEPARTMENT_KEY = "department";
	private static final String TIMEZONE_KEY = "timezone";
	private static final String APP_ADMIN = "appAdmin";

	private final RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AccountService accountService;

	@Override
	public AppDirectIntegrationAPI getAppDirectIntegrationApi(String basePath, ApplicationProfile applicationProfile) {
		AppDirectIntegrationAPI api = JAXRSClientFactory.create(basePath, AppDirectIntegrationAPI.class);
		ClientConfiguration config = WebClient.getConfig(api);
		config.getOutInterceptors().add(new OAuthPhaseInterceptor(applicationProfile.getOauthConsumerKey(), applicationProfile.getOauthConsumerSecret()));
		return api;
	}

	@Override
	@Transactional
	public APIResult processEvent(ApplicationProfile applicationProfile, String eventUrl, String token) {
		String basePath;
		if (applicationProfile.isLegacy()) {
			basePath = applicationProfile.getLegacyMarketplaceBaseUrl();
		} else {
			basePath = IntegrationUtils.extractBasePath(eventUrl);
			token = IntegrationUtils.extractToken(eventUrl);
		}
		Preconditions.checkState(StringUtils.isNotBlank(basePath), "basePath should not be blank");
		Preconditions.checkState(StringUtils.isNotBlank(token), "token should not be blank");

		AppDirectIntegrationAPI api = getAppDirectIntegrationApi(basePath, applicationProfile);

		EventInfo eventInfo = api.readEvent(token);
		if (eventInfo == null || eventInfo.getType() == null) {
			return new APIResult(false, ErrorCode.UNKNOWN_ERROR, "Event info not found or invalid.");
		}
		if (StringUtils.isNotBlank(eventUrl) && !basePath.equals(eventInfo.getMarketplace().getBaseUrl())) {
			return new APIResult(false, ErrorCode.UNKNOWN_ERROR, "Event partner mismatch.");
		}
		switch(eventInfo.getType()) {
			case SUBSCRIPTION_ORDER:
				return processSubscriptionOrderEvent(basePath, eventInfo, applicationProfile);
			case SUBSCRIPTION_CHANGE:
				return processSubscriptionChangeEvent(basePath, eventInfo, applicationProfile);
			case SUBSCRIPTION_CANCEL:
				return processSubscriptionCancelEvent(eventInfo, applicationProfile);
			case USER_ASSIGNMENT:
				return processUserAssignmentEvent(eventInfo, applicationProfile);
			case USER_UNASSIGNMENT:
				return processUserUnassignmentEvent(eventInfo, applicationProfile);
			case SUBSCRIPTION_NOTICE:
				return processSubscriptionNoticeEvent(eventInfo);
			case ADDON_ORDER:
				return processAddonOrderEvent(eventInfo, applicationProfile);
			case ADDON_CHANGE:
				return processAddonChangeEvent(eventInfo, applicationProfile);
			case ADDON_CANCEL:
				return processAddonCancelEvent(eventInfo);
			default:
				return new APIResult(false, ErrorCode.UNKNOWN_ERROR, "Event type not supported by this endpoint: " + String.valueOf(eventInfo.getType()));
		}
	}

	private APIResult processSubscriptionOrderEvent(String appDirectBaseUrl, EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_ORDER);
		final APIResult result;
		// Create the account.
		UserBean adminBean = new UserBean();
		adminBean.setUuid(eventInfo.getCreator().getUuid());
		adminBean.setOpenId(eventInfo.getCreator().getOpenId());
		adminBean.setEmail(eventInfo.getCreator().getEmail());
		User existingUser = userRepository.findByOpenId(adminBean.getOpenId());
		if (existingUser != null) {
			result = new APIResult(false, ErrorCode.USER_ALREADY_EXISTS, "An account with this user already exists.");
		} else {
			adminBean.setFirstName(eventInfo.getCreator().getFirstName());
			adminBean.setLastName(eventInfo.getCreator().getLastName());
			adminBean.setAdmin(true);
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setUuid(eventInfo.getPayload().getCompany().getUuid());
			accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
			accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
			accountBean.setAppDirectBaseUrl(appDirectBaseUrl);
			if (eventInfo.hasLink(SAML_IDP_LINK)) {
				fetchSamlIdpSettings(accountBean, eventInfo.getLink(SAML_IDP_LINK).getHref(), applicationProfile);
			}
			accountService.createAccount(accountBean, adminBean);
			result = new APIResult(true, "Account created successfully.");
			result.setAccountIdentifier(accountBean.getUuid());
		}
		return result;
	}

	private void fetchSamlIdpSettings(AccountBean accountBean, String samlIdpUrl, ApplicationProfile applicationProfile) {
		OAuthUrlSigner oauthUrlSigner = new OAuthUrlSignerImpl(applicationProfile.getOauthConsumerKey(), applicationProfile.getOauthConsumerSecret());
		URI signedIdpUri = URI.create(oauthUrlSigner.sign(samlIdpUrl + ".json"));
		SamlRelyingPartyWS idp = restTemplate.getForObject(signedIdpUri, SamlRelyingPartyWS.class);
		accountBean.setSamlIdpEntityId(idp.getIdpIdentifier());
		accountBean.setSamlIdpMetadataUrl(samlIdpUrl + ".samlmetadata.xml");
	}

	private APIResult processSubscriptionChangeEvent(String appDirectBaseUrl, EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_CHANGE);
		APIResult result;
		try {
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			accountBean.setAppDirectBaseUrl(appDirectBaseUrl);
			accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
			accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
			accountService.update(accountBean);
			result = new APIResult(true, String.format("Successfully updated account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		} catch (ObjectNotFoundException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
		return result;
	}

	private APIResult processSubscriptionCancelEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_CANCEL);
		APIResult result;
		try {
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			accountService.delete(accountBean);
			result = new APIResult(true, String.format("Successfully deleted account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		} catch (ObjectNotFoundException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
		return result;
	}

	private APIResult processUserAssignmentEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.USER_ASSIGNMENT);
		APIResult result;
		AccountBean accountBean = new AccountBean(applicationProfile);
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
		User existingUser = userRepository.findByOpenId(userBean.getOpenId());
		if (existingUser != null) {
			if (StringUtils.equals(existingUser.getAccount().getUuid(), accountBean.getUuid())) {
				result = new APIResult(true, "A user with this OpenID already exists but was mapped correctly.");
			} else {
				// A user with the same OpenID already exists in a different account.
				// Fail.
				result = new APIResult(false, ErrorCode.USER_ALREADY_EXISTS, "A user with this OpenID or email already exists.");
			}
		} else {
			try {
				// Create the new user.
				accountService.createUser(userBean, accountBean);
				result = new APIResult(true, "Successfully created user: " + userBean.getUuid());
			} catch (ObjectNotFoundException onfe) {
				// The account could not be found. Fail.
				result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, onfe.getMessage());
			}
		}
		return result;
	}

	private APIResult processUserUnassignmentEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.USER_UNASSIGNMENT);
		APIResult result;
		try {
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			User user = readUserByOpenID(eventInfo.getPayload().getUser().getOpenId());
			if (!StringUtils.equals(accountBean.getUuid(), user.getAccount().getUuid())) {
				// The user account is not the same as the account passed
				// in. We can't allow that. Fail.
				result = new APIResult(false, ErrorCode.UNAUTHORIZED, "User does not belong to the expected account.");
			} else {
				user.getAccount().getUsers().remove(user);
				user.setAccount(null);
				this.userRepository.delete(user);
				result = new APIResult(true, "Successfully deleted user: " + eventInfo.getPayload().getUser().getOpenId());
			}
		} catch (ObjectNotFoundException onfe) {
			// The user could not be found. Fail.
			result = new APIResult(false, ErrorCode.USER_NOT_FOUND, onfe.getMessage());
		}
		return result;
	}

	private APIResult processSubscriptionNoticeEvent(EventInfo eventInfo) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_NOTICE);
		return new APIResult(true, "Dummy notice success.");
	}

	private APIResult processAddonOrderEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.ADDON_ORDER);
		APIResult result;
		try {
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			AddonBean addonBean = new AddonBean();
			OrderInfo orderInfo = eventInfo.getPayload().getOrder();
			addonBean.setCode(orderInfo.getAddonOfferingCode());
			addonBean.setAddonIdentifier(String.format("%s-%s-%s", accountBean.getUuid(), addonBean.getCode(), Time.now()));
			if (!orderInfo.getItems().isEmpty()) {
				addonBean.setQuantity(orderInfo.getItems().get(0).getQuantity());
			}
			accountService.createAddon(addonBean, accountBean);
			result = new APIResult(true, "Addon created successfully.");
			result.setId(addonBean.getAddonIdentifier());
		} catch (ObjectNotFoundException onfe) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, onfe.getMessage());
		}
		return result;
	}

	private APIResult processAddonChangeEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.ADDON_CHANGE);
		APIResult result;
		try {
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
			AddonBean addonBean = new AddonBean();
			OrderInfo orderInfo = eventInfo.getPayload().getOrder();
			addonBean.setCode(orderInfo.getAddonOfferingCode());
			addonBean.setAddonIdentifier(eventInfo.getPayload().getAddonInstance().getId());
			addonBean.setQuantity(orderInfo.getItems().get(0).getQuantity());
			accountService.updateAddon(addonBean);
			result = new APIResult(true, "Addon changed successfully.");
		} catch (ObjectNotFoundException onfe) {
			result = new APIResult(false, ErrorCode.UNKNOWN_ERROR, onfe.getMessage());
		}
		return result;
	}

	private APIResult processAddonCancelEvent(EventInfo eventInfo) {
		Preconditions.checkState(eventInfo.getType() == EventType.ADDON_CANCEL);
		APIResult result;
		try {
			AddonBean addonBean = new AddonBean();
			addonBean.setAddonIdentifier(eventInfo.getPayload().getAddonInstance().getId());
			accountService.deleteAddon(addonBean);
			result = new APIResult(true, String.format("Successfully cancel addon: %s", eventInfo.getPayload().getAddonInstance().getId()));
		} catch (ObjectNotFoundException onfe) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, onfe.getMessage());
		}
		return result;
	}

	private User readUserByOpenID(String openId) {
		User user = userRepository.findByOpenId(openId);
		if (user == null) {
			throw new ObjectNotFoundException(openId, User.class.toString());
		}
		return user;
	}
}
