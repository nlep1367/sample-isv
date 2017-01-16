package com.appdirect.isv.integration.wicket.pages.billing;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.AddonBean;
import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.type.PricingUnit;
import com.appdirect.isv.integration.remote.vo.AccountInfo;
import com.appdirect.isv.integration.remote.vo.AddonInstanceInfo;
import com.appdirect.isv.integration.remote.vo.BillingAPIResult;
import com.appdirect.isv.integration.remote.vo.UsageBean;
import com.appdirect.isv.integration.remote.vo.UsageItemBean;
import com.appdirect.isv.integration.service.IntegrationService;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;
import com.appdirect.isv.web.wicket.session.WicketSession;

@AuthorizeInstantiation("USER")
@MountPath("/usage")
public class UsagePage extends BaseWebPage {
	private static final long serialVersionUID = 3984244051932028004L;

	@SpringBean
	private AccountService accountService;
	@SpringBean
	private IntegrationService integrationService;

	public UsagePage(PageParameters parameters) {
		super(parameters);
		final UsageBean usageBean = new UsageBean();
		final AccountBean accountBean = accountService.readUserAccount(WicketSession.get().getCurrentUser().getId());
		AccountInfo accountInfo = new AccountInfo();
		accountInfo.setAccountIdentifier(accountBean.getId().toString());
		usageBean.setAccount(accountInfo);
		UsageItemBean usageItemBean = new UsageItemBean();
		usageBean.getItems().add(usageItemBean);
		Form<Void> usageForm = new Form<>("usageForm");
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		usageForm.add(feedback.setOutputMarkupId(true));
		usageForm.add(new Label("accountIdentifier", new PropertyModel<String>(usageBean, "account.accountIdentifier")));
		usageForm.add(new DropDownChoice<>("usageUnit", new PropertyModel<PricingUnit>(usageItemBean, "unit"), Arrays.asList(PricingUnit.values())));
		usageForm.add(new TextArea<>("usageQuantity", new PropertyModel<BigDecimal>(usageItemBean, "quantity")));
		usageForm.add(new TextArea<>("usagePrice", new PropertyModel<BigDecimal>(usageItemBean, "price")));
		usageForm.add(new TextArea<>("usageDescription", new PropertyModel<String>(usageItemBean, "description")));
		usageForm.add(new AjaxButton("report") {
			private static final long serialVersionUID = -5534177485294277213L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				AppDirectIntegrationAPI api = integrationService.getAppDirectIntegrationApi(accountBean.getAppDirectBaseUrl(), accountBean.getApplicationProfile());
				BillingAPIResult result = api.billUsage(usageBean);
				info(String.format("success: %s, message: %s", result.isSuccess(), result.getMessage()));
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedback);
			}
		});
		add(usageForm);

		final ListView<AddonBean> addons = new ListView<AddonBean>("addons", accountBean.getAddons()) {
			private static final long serialVersionUID = 8367680256428557102L;

			@Override
			protected void populateItem(ListItem<AddonBean> item) {
				final UsageBean addonUsageBean = new UsageBean();

				AccountInfo accountInfo = new AccountInfo();
				accountInfo.setAccountIdentifier(accountBean.getAppDirectUuid());
				addonUsageBean.setAccount(accountInfo);

				AddonInstanceInfo addonAccountInfo = new AddonInstanceInfo();
				addonAccountInfo.setId(item.getModelObject().getId().toString());
				addonUsageBean.setAddonInstance(addonAccountInfo);

				UsageItemBean addonItemBean = new UsageItemBean();
				addonUsageBean.getItems().add(addonItemBean);
				Form<Void> addonUsageForm = new Form<>("addonUsageForm");
				final FeedbackPanel addonFeedback = new FeedbackPanel("addonFeedback");
				addonUsageForm.add(addonFeedback.setOutputMarkupId(true));
				addonUsageForm.add(new Label("addonCode", new PropertyModel<String>(item.getModelObject(), "code")));
				addonUsageForm.add(new Label("addonIdentifier", new PropertyModel<String>(addonUsageBean, "addonInstance.id")));
				addonUsageForm.add(new DropDownChoice<>("addonUnit", new PropertyModel<PricingUnit>(addonItemBean, "unit"), Arrays.asList(PricingUnit.values())).setOutputMarkupId(true));
				addonUsageForm.add(new TextArea<>("addonQuantity", new PropertyModel<BigDecimal>(addonItemBean, "quantity")).setOutputMarkupId(true));
				addonUsageForm.add(new AjaxButton("addonReport") {
					private static final long serialVersionUID = -6560990507356150457L;

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						AppDirectIntegrationAPI api = integrationService.getAppDirectIntegrationApi(accountBean.getAppDirectBaseUrl(), accountBean.getApplicationProfile());
						BillingAPIResult result = api.billUsage(addonUsageBean);
						info(String.format("success: %s, message: %s", result.isSuccess(), result.getMessage()));
						target.add(addonFeedback);
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						target.add(addonFeedback);
					}
				});
				item.add(addonUsageForm);
			}
		};
		add(addons);
	}
}
