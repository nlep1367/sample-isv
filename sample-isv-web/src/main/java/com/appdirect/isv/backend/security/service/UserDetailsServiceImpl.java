package com.appdirect.isv.backend.security.service;

import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.backend.core.dao.GenericDAO;
import com.appdirect.isv.backend.user.model.User;
import com.appdirect.isv.backend.user.vo.UserBean;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private GenericDAO<User, Long> userDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class).add(Restrictions.eq("username", username));
		List<User> result = this.userDao.findByCriteria(User.class, criteria);
		if (result == null || result.size() != 1) {
			throw new ObjectNotFoundException(username, User.class.toString());
		} else {
			User user = result.get(0);
			UserBean userBean = new UserBean(user);
			return new UserDetailsImpl(userBean);
		}
	}
}
