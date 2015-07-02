package com.appdirect.isv.web.wicket.pages;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.model.AuthenticationMethod;
import com.appdirect.isv.repository.ApplicationProfileRepository;
import com.google.common.collect.Lists;

@MountPath("/applicationProfiles")
public class ApplicationProfilesPage extends BaseWebPage {
	private static final long serialVersionUID = -7983444720628296776L;

	@SpringBean
	private ApplicationProfileRepository applicationProfileRepository;

	public ApplicationProfilesPage(PageParameters parameters) {
		super(parameters);
		Form<Void> form = new Form<>("form");
		DataView<ApplicationProfile> dataView = new DataView<ApplicationProfile>("row", new ApplicationProfileDataProvider()) {
			private static final long serialVersionUID = 1935452003342829633L;

			@Override
			protected void populateItem(Item<ApplicationProfile> item) {
				ApplicationProfile applicationProfile = item.getModelObject();
				item.add(new TextField<>("url", new PropertyModel<String>(applicationProfile, "url")));
				item.add(new TextField<>("oauthConsumerKey", new PropertyModel<String>(applicationProfile, "oauthConsumerKey")));
				item.add(new TextField<>("oauthConsumerSecret", new PropertyModel<String>(applicationProfile, "oauthConsumerSecret")));
				item.add(new DropDownChoice<>("authenticationMethod", new PropertyModel<AuthenticationMethod>(applicationProfile, "authenticationMethod"), Arrays.asList(AuthenticationMethod.values())));
				item.add(new CheckBox("legacy", new PropertyModel<Boolean>(applicationProfile, "legacy")));
				item.add(new TextField<>("legacyMarketplaceBaseUrl", new PropertyModel<String>(applicationProfile, "legacyMarketplaceBaseUrl")));
				item.add(new AjaxButton("save") {
					private static final long serialVersionUID = -2906135886654647557L;

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						applicationProfileRepository.save(applicationProfile);
						form.info("Successfully saved " + applicationProfile.getOauthConsumerKey() + ".");
						target.add(form, getFeedbackPanel());
					}
				});
				item.add(new AjaxButton("delete") {
					private static final long serialVersionUID = 4150083595321796462L;

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						applicationProfileRepository.delete(applicationProfile);
						form.info("Successfully deleted " + applicationProfile.getOauthConsumerKey() + ".");
						target.add(form, getFeedbackPanel());
					}
				});
			}
		};
		form.add(dataView);
		form.add(new AjaxButton("add") {
			private static final long serialVersionUID = 6632179340203735489L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				ApplicationProfile applicationProfile = new ApplicationProfile();
				applicationProfile.setUuid(UUID.randomUUID().toString());
				applicationProfile.setOauthConsumerKey(applicationProfile.getUuid());
				applicationProfileRepository.save(applicationProfile);
				form.info("Successfully added " + applicationProfile.getOauthConsumerKey() + ".");
				target.add(form, getFeedbackPanel());
			}
		});
		add(form.setOutputMarkupId(true));
	}

	private static class ApplicationProfileDataProvider extends ListDataProvider<ApplicationProfile> {
		private static final long serialVersionUID = -8762381320766663521L;

		@SpringBean
		private ApplicationProfileRepository applicationProfileRepository;

		public ApplicationProfileDataProvider() {
			super();
			Injector.get().inject(this);
		}

		@Override
		protected List<ApplicationProfile> getData() {
			return Lists.newArrayList(applicationProfileRepository.findAll());
		}
	}
}
