package com.bankingsystem.security;

import com.bankingsystem.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails {
    private Long id;
    private Long customerId;
    private String username;
    private String password;
    private boolean locked;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal build(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
        Long customerId = user.getCustomer() != null ? user.getCustomer().getCustomerId() : null;
        return new UserPrincipal(
                user.getUserId(),
                customerId,
                user.getUsername(),
                user.getPassword(),
                user.isAccountLocked(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !locked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
