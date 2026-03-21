package com.example.commonsystem.security;

import com.example.commonsystem.user.domain.User;
import com.example.commonsystem.user.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserMapper userMapper;

  public UserDetailsServiceImpl(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userMapper.findByUsername(username);
    if (user == null || !user.enabled()) {
      throw new UsernameNotFoundException("User not found");
    }
    return new UserPrincipal(
        user.userId(),
        user.username(),
        user.passwordHash(),
        user.roleKey(),
        user.name(),
        user.orgId(),
        user.tenantId(),
        user.enabled()
    );
  }
}
