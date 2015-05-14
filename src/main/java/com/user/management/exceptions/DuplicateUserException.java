package com.user.management.exceptions;


public class DuplicateUserException extends BaseWebApplicationException {

    public DuplicateUserException() {
        super(409, "40901", "User already exists", "An attempt was made to create a user that already exists");
    }
}
