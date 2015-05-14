package com.user.management.services;

import com.user.management.domain.AuthorizationToken;
import com.user.management.domain.Role;
import com.user.management.domain.User;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.LoginRequest;
import com.user.management.rest.api.UpdateUserRequest;
import org.springframework.social.connect.Connection;

public interface UserService {

    /**
     * Create a new User with the given role
     *
     * @param request
     * @param role
     * @return AuthenticatedUserToken
     */
    public AuthenticatedUserToken createUser(CreateUserRequest request, Role role);


    /**
     * Create a Default User with a given role
     *
     * @param role
     * @return AuthenticatedUserToken
     */
    public AuthenticatedUserToken createUser(Role role);

    /*
    /**
     * Login a User
     *
     * @param request
     * @return AuthenticatedUserToken
     */
    public AuthenticatedUserToken login(LoginRequest request);

    /**
     * Log in a User using Connection details from an authorized request from the User's supported Social provider
     * encapsulated in the {@link org.springframework.social.connect.Connection} parameter
     *
     * @param connection containing the details of the authorized user account form the Social provider
     * @return the User account linked to the {@link com.porterhead.rest.user.domain.SocialUser} account
     */
    public AuthenticatedUserToken socialLogin(Connection<?> connection);

    /**
     * Get a User based on a unique identifier
     *
     * Identifiers supported are uuid, emailAddress
     *
     * @param userIdentifier
     * @return  User
     */
    public ExternalUser getUser(ExternalUser requestingUser, String userIdentifier);

    /**
     * Delete user, only authenticated user accounts can be deleted
     *
     * @param userMakingRequest the user authorized to delete the user
     * @param userId the id of the user to delete
     */
    public void deleteUser(ExternalUser userMakingRequest, String userId);

    /**
     * Save User
     *
     * @param userId
     * @param request
     */
    public ExternalUser saveUser(String userId, UpdateUserRequest request);

    /**
     * Create an AuthorizationToken for the User
     *
     * @return
     */
    public AuthorizationToken createAuthorizationToken(User user);


}
