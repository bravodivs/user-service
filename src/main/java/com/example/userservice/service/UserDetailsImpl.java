package com.example.userservice.service;

import com.example.userservice.model.UserDto;
import com.example.userservice.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.yaml.snakeyaml.util.EnumUtils;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImpl.class);

    private final UserDto userDto;

    public UserDetailsImpl(UserDto userDto) {
        this.userDto = userDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<? extends GrantedAuthority> authorityList = userDto.getRole().stream()
                .map(role -> {
                    UserRole t = EnumUtils.findEnumInsensitiveCase(UserRole.class, role);
                    return new SimpleGrantedAuthority(t.name());
                })
                .toList();
        logger.info("User {} has these roles- {}", userDto.getUsername(), authorityList.stream().toList());
        return authorityList;
    }

    @Override
    public String getPassword() {
        return userDto.getPassword();
    }

    @Override
    public String getUsername() {
        return userDto.getUsername();
    }

    public List<String> getRoles() {
        return userDto.getRole();
    }

    public String getEmail() {
        return userDto.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userDto.getIsEnabled();
    }
}
