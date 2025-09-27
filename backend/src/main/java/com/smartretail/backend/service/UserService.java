package com.smartretail.backend.service;

import com.smartretail.backend.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User getUserById(String id);
}