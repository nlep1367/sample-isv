package com.appdirect.isv.security.openid;

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
		User user = userRepository.findByOpenId(openId);
		if (user == null) {
			throw new UsernameNotFoundException(openId);
		}
		UserBean userBean = new UserBean(user);
		return new UserDetailsImpl(userBean);
	}
}
