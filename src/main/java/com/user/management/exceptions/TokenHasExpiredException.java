package com.user.management.exceptions;


/**
 *
 * @version 1.0
 * @author: Iain Porter iain.porter@porterhead.com
 * @since 14/09/2012
 */
public class TokenHasExpiredException extends BaseWebApplicationException {

    public TokenHasExpiredException() {
        super(403, "40304", "Token has expired", "An attempt was made to load a token that has expired");
    }
}
