package com.user.management.authorization.impl;

import com.user.management.authorization.AuthorizationRequestContext;
import com.user.management.authorization.AuthorizationService;
import com.user.management.domain.AuthorizationToken;
import com.user.management.domain.User;
import com.user.management.exceptions.AuthorizationException;
import com.user.management.repositories.UserRepository;
import com.user.management.rest.api.ExternalUser;

/**
 *
 * Simple authorization service that requires a session token in the Authorization header
 * This is then matched to a user
 *
 * @version 1.0
 * @author: Iain Porter
 * @since 29/01/2013
 */
public class SessionTokenAuthorizationService implements AuthorizationService {

    /**
     * directly access user objects
     */
    private final UserRepository userRepository;

    public SessionTokenAuthorizationService(UserRepository repository) {
        this.userRepository = repository;
    }

    public ExternalUser authorize(AuthorizationRequestContext securityContext) {
        String token = securityContext.getAuthorizationToken();
        ExternalUser externalUser = null;
        if(token == null) {
            return externalUser;
        }
        User user =  userRepository.findBySession(token);
        if(user == null) {
            throw new AuthorizationException("Session token not valid");
        }
        AuthorizationToken authorizationToken = user.getAuthorizationToken();
            if (authorizationToken.getToken().equals(token)) {
                externalUser = new ExternalUser(user);
            }
        return externalUser;
    }
}
