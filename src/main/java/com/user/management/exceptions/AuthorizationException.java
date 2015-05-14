package com.user.management.exceptions;



/**
 * User: porter
 * Date: 04/04/2012
 * Time: 15:32
 */
public class AuthorizationException extends BaseWebApplicationException {

    public AuthorizationException(String applicationMessage) {
        super(403, "40301", "Not authorized", applicationMessage);
    }

}
