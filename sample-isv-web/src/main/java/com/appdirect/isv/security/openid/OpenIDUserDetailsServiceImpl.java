package com.appdirect.isv.security.openid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.model.User;
import com.appdirect.isv.repository.UserRepository;
import com.appdirect.isv.security.UserDetailsImpl;

public class OpenIDUserDetailsServiceImpl implements AuthenticationUserDetailsService<OpenIDAuthenticationToken> {
	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
		String openId = token.getIdentityUrl();
		List<User> users = userRepository.findByAppDirectOpenId(openId);
		if (users.isEmpty()) {
			throw new UsernameNotFoundException(openId);
		}
		UserBean userBean = new UserBean(users.get(0));
		return new UserDetailsImpl(userBean);
	}
}
