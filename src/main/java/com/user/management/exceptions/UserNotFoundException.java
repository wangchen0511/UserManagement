package com.user.management.exceptions;

/**
 *
 * @version 1.0
 * @author: Iain Porter iain.porter@porterhead.com
 * @since 12/09/2012
 */
public class UserNotFoundException extends BaseWebApplicationException {

    public UserNotFoundException() {
        super(404, "40402", "User Not Found", "No User could be found for that Id");
    }
}
