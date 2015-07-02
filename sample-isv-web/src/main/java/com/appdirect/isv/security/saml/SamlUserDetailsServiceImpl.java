package com.appdirect.isv.security.saml;

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
		String userUuid = credential.getNameID().getValue();
		User user = userRepository.findByUuid(userUuid);
		if (user == null) {
			throw new UsernameNotFoundException(userUuid);
		}
		UserBean userBean = new UserBean(user);
		return new UserDetailsImpl(userBean);
	}
}