package com.user.management.services;

import org.springframework.beans.factory.annotation.Autowired;

import com.user.management.config.ApplicationConfig;
import com.user.management.repositories.UserRepository;

/**
 * User: porter
 * Date: 04/04/2012
 * Time: 14:21
 */
public class BaseServiceTest {

    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ApplicationConfig applicationConfig;


}
