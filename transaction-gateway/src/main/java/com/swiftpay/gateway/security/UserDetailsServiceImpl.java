package com.swiftpay.gateway.security;

import com.swiftpay.gateway.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())))
                .accountExpired(!account.isActive())
                .credentialsExpired(false)
                .disabled(!account.isActive())
                .build();
    }
}