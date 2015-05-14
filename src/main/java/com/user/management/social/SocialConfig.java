package com.user.management.social;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

import com.user.management.config.ApplicationConfig;
import com.user.management.repositories.SocialUserRepository;
import com.user.management.repositories.UserRepository;

/**
 * User: porter
 * Date: 13/03/2012
 * Time: 17:39
 */
@Configuration
public class SocialConfig {

    @Autowired
    ApplicationConfig config;

    @Autowired
    SocialUserRepository socialUserRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TextEncryptor textEncryptor;

    @Bean
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        registry.addConnectionFactory(new FacebookConnectionFactory(
            config.getFacebookClientId(),
            config.getFacebookClientSecret()));
        return registry;
    }

    @Bean
    public UsersConnectionRepository usersConnectionRepository() {
        JpaUsersConnectionRepository usersConnectionRepository = new JpaUsersConnectionRepository(socialUserRepository, userRepository,
                connectionFactoryLocator(), textEncryptor);

        return usersConnectionRepository;
    }
}