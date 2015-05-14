package com.user.management.authorization.impl;

import com.user.management.authorization.exception.InvalidAuthorizationHeaderException;
import com.user.management.domain.Role;
import com.user.management.rest.api.ExternalUser;

import javax.ws.rs.core.SecurityContext;

import java.security.Principal;

/**
 * Implementation of {@link javax.ws.rs.core.SecurityContext}
 *
 * User: porter
 * Date: 16/03/2012
 * Time: 16:13
 */
public class SecurityContextImpl implements SecurityContext {

    private final ExternalUser user;

    public SecurityContextImpl(ExternalUser user) {
        this.user = user;
    }

    public Principal getUserPrincipal() {
        return user;
    }

    public boolean isUserInRole(String role) {
        if(role.equalsIgnoreCase(Role.anonymous.name())) {
             return true;
        }
        if(user == null) {
            throw new InvalidAuthorizationHeaderException();
        }
        return user.getRole().equalsIgnoreCase(role);
    }

    public boolean isSecure() {
        return false;
    }

    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}
