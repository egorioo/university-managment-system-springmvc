package spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spring.controllers.DefaultController;
import spring.security.dao.UserDAO;
import spring.security.model.User;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;

    @Autowired
    public UserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        User user = userDAO.findByEmail(email);
        if (user.getLogin() == null) {
            throw new UsernameNotFoundException("User doesn't exists");
        }
        return SecurityUser.fromUser(user);
    }
}
