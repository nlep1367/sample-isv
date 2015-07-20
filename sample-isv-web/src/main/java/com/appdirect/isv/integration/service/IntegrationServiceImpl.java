package com.appdirect.isv.integration.service;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
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
import com.appdirect.isv.integration.remote.type.NoticeType;
import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.integration.remote.vo.OrderInfo;
import com.appdirect.isv.integration.remote.vo.UserInfo;
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
				return processSubscriptionNoticeEvent(eventInfo, applicationProfile);
			case ADDON_ORDER:
				return processAddonOrderEvent(eventInfo, applicationProfile);
			case ADDON_CHANGE:
				return processAddonChangeEvent(eventInfo, applicationProfile);
			case ADDON_CANCEL:
				return processAddonCancelEvent(eventInfo, applicationProfile);
			default:
				return new APIResult(false, ErrorCode.UNKNOWN_ERROR, "Event type not supported by this endpoint: " + String.valueOf(eventInfo.getType()));
		}
	}

	private APIResult processSubscriptionOrderEvent(String appDirectBaseUrl, EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_ORDER);

		UserBean adminBean = new UserBean();
		adminBean.setAppDirectUuid(eventInfo.getCreator().getUuid());
		adminBean.setAppDirectOpenId(eventInfo.getCreator().getOpenId());
		adminBean.setEmail(eventInfo.getCreator().getEmail());
		adminBean.setFirstName(eventInfo.getCreator().getFirstName());
		adminBean.setLastName(eventInfo.getCreator().getLastName());
		adminBean.setAdmin(true);

		AccountBean accountBean = new AccountBean(applicationProfile);
		accountBean.setAppDirectUuid(eventInfo.getPayload().getCompany().getUuid());
		accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
		accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
		accountBean.setAppDirectBaseUrl(appDirectBaseUrl);

		if (eventInfo.hasLink(SAML_IDP_LINK)) {
			fetchSamlIdpSettings(accountBean, eventInfo.getLink(SAML_IDP_LINK).getHref(), applicationProfile);
		}

		accountService.createAccount(accountBean, adminBean);

		APIResult result = new APIResult(true, "Account created successfully.");
		result.setAccountIdentifier(accountBean.getId().toString());
		result.setUserIdentifier(adminBean.getId().toString());
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
			Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
			AccountBean accountBean = new AccountBean(applicationProfile);
			accountBean.setId(accountId);
			accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
			accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
			accountService.updateAccount(accountBean);
			result = new APIResult(true, String.format("Successfully updated account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		} catch (ObjectNotFoundException | NumberFormatException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
		return result;
	}

	private APIResult processSubscriptionCancelEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_CANCEL
				|| eventInfo.getType() == EventType.SUBSCRIPTION_NOTICE && eventInfo.getPayload().getNotice().getType() == NoticeType.CLOSED);
		APIResult result;
		try {
			Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
			accountService.deleteAccount(accountId);
			result = new APIResult(true, String.format("Successfully deleted account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		} catch (ObjectNotFoundException | NumberFormatException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
		return result;
	}

	private APIResult processUserAssignmentEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.USER_ASSIGNMENT);
		APIResult result;
		try {
			// Create the new user.
			UserInfo userInfo = eventInfo.getPayload().getUser();
			UserBean userBean = buildUserBean(userInfo);
			Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
			accountService.createUser(userBean, accountId);
			result = new APIResult(true, "Successfully created user: " + userBean.getAppDirectUuid());
			result.setUserIdentifier(userBean.getId().toString());
		} catch (ObjectNotFoundException | NumberFormatException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, String.format("Could not find account with identifier %s", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		}
		return result;
	}

	private UserBean buildUserBean(UserInfo userInfo) {
		UserBean userBean = new UserBean();
		userBean.setAppDirectUuid(userInfo.getUuid());
		userBean.setAppDirectOpenId(userInfo.getOpenId());
		userBean.setEmail(userInfo.getEmail());
		userBean.setFirstName(userInfo.getFirstName());
		userBean.setLastName(userInfo.getLastName());
		boolean admin = false;
		if (userInfo.getAttributes() != null) {
			userBean.setZipCode(userInfo.getAttributes().get(ZIP_CODE_KEY));
			userBean.setDepartment(userInfo.getAttributes().get(DEPARTMENT_KEY));
			userBean.setTimezone(userInfo.getAttributes().get(TIMEZONE_KEY));
			admin = Boolean.parseBoolean(userInfo.getAttributes().get(APP_ADMIN));
		}
		userBean.setAdmin(admin);
		return userBean;
	}

	private APIResult processUserUnassignmentEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.USER_UNASSIGNMENT);
		APIResult result;
		try {
			Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
			AccountBean accountBean = accountService.readAccount(accountId);
			String appDirectUuid = eventInfo.getPayload().getUser().getUuid();
			UserBean userBean = getUserByAppDirectUuid(accountBean, appDirectUuid);
			accountService.deleteUser(userBean.getId());
			result = new APIResult(true, "Successfully deleted user: " + appDirectUuid);
		} catch (ObjectNotFoundException | NumberFormatException e) {
			// The user could not be found. Fail.
			result = new APIResult(false, ErrorCode.USER_NOT_FOUND, e.getMessage());
		}
		return result;
	}

	private UserBean getUserByAppDirectUuid(AccountBean accountBean, String appDirectUuid) {
		for (UserBean userBean : accountBean.getUsers()) {
			if (userBean.getAppDirectUuid().equals(appDirectUuid)) {
				return userBean;
			}
		}
		throw new ObjectNotFoundException(appDirectUuid, User.class.toString());
	}

	private APIResult processSubscriptionNoticeEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.SUBSCRIPTION_NOTICE);
		if (eventInfo.getPayload().getNotice().getType() == NoticeType.CLOSED) {
			return processSubscriptionCancelEvent(eventInfo, applicationProfile);
		} else {
			return new APIResult(true, "Dummy notice success.");
		}
	}

	private APIResult processAddonOrderEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.ADDON_ORDER);
		APIResult result;
		try {
			OrderInfo orderInfo = eventInfo.getPayload().getOrder();
			AddonBean addonBean = buildAddonBean(orderInfo);
			Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
			accountService.createAddon(addonBean, accountId);
			result = new APIResult(true, "Addon created successfully.");
			result.setId(addonBean.getId().toString());
		} catch (ObjectNotFoundException | NumberFormatException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, e.getMessage());
		}
		return result;
	}

	private AddonBean buildAddonBean(OrderInfo orderInfo) {
		AddonBean addonBean = new AddonBean();
		addonBean.setCode(orderInfo.getAddonOfferingCode());
		if (!orderInfo.getItems().isEmpty()) {
			addonBean.setQuantity(orderInfo.getItems().get(0).getQuantity());
		}
		return addonBean;
	}

	private APIResult processAddonChangeEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.ADDON_CHANGE);
		APIResult result;
		try {
			OrderInfo orderInfo = eventInfo.getPayload().getOrder();
			long addonId = Long.parseLong(eventInfo.getPayload().getAddonInstance().getId());
			AddonBean addonBean = new AddonBean();
			addonBean.setId(addonId);
			addonBean.setCode(orderInfo.getAddonOfferingCode());
			addonBean.setQuantity(orderInfo.getItems().get(0).getQuantity());
			accountService.updateAddon(addonBean);
			result = new APIResult(true, "Addon changed successfully.");
		} catch (ObjectNotFoundException | NumberFormatException e) {
			result = new APIResult(false, ErrorCode.UNKNOWN_ERROR, e.getMessage());
		}
		return result;
	}

	private APIResult processAddonCancelEvent(EventInfo eventInfo, ApplicationProfile applicationProfile) {
		Preconditions.checkState(eventInfo.getType() == EventType.ADDON_CANCEL);
		APIResult result;
		try {
			long addonId = Long.parseLong(eventInfo.getPayload().getAddonInstance().getId());
			accountService.deleteAddon(addonId);
			result = new APIResult(true, String.format("Successfully cancel addon: %s", eventInfo.getPayload().getAddonInstance().getId()));
		} catch (ObjectNotFoundException | NumberFormatException e) {
			result = new APIResult(false, ErrorCode.ACCOUNT_NOT_FOUND, e.getMessage());
		}
		return result;
	}
}
