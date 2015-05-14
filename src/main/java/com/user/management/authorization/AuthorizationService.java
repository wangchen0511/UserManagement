package com.user.management.authorization;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import com.user.management.rest.api.ExternalUser;


/**
 *
 * @author: Iain Porter
 */
public interface AuthorizationService {

    /**
     * Given an AuthorizationRequestContext validate and authorize a User
     *
     * @param authorizationRequestContext the context required to authorize a user for a particular request
     * @return ExternalUser
     * @throws UnsupportedEncodingException 
     * @throws NoSuchAlgorithmException 
     */
    public ExternalUser authorize(AuthorizationRequestContext authorizationRequestContext) throws NoSuchAlgorithmException, UnsupportedEncodingException;
}
