package com.appdirect.isv.security.saml;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.model.User;
import com.appdirect.isv.repository.UserRepository;
import com.appdirect.isv.security.UserDetailsImpl;

public class SamlUserDetailsServiceImpl implements SAMLUserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		String appDirectUuid = credential.getNameID().getValue();
		List<User> users = userRepository.findByAppDirectUuid(appDirectUuid);
		if (users.isEmpty()) {
			throw new UsernameNotFoundException(appDirectUuid);
		}
		UserBean userBean = new UserBean(users.get(0));
		return new UserDetailsImpl(userBean);
	}
}