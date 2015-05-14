package com.user.management.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.user.management.config.ApplicationConfig;
import com.user.management.domain.AuthorizationToken;
import com.user.management.domain.Role;
import com.user.management.domain.User;
import com.user.management.exceptions.AuthenticationException;
import com.user.management.exceptions.AuthorizationException;
import com.user.management.exceptions.DuplicateUserException;
import com.user.management.exceptions.UserNotFoundException;
import com.user.management.repositories.UserRepository;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.LoginRequest;
import com.user.management.rest.api.UpdateUserRequest;
import com.user.management.social.JpaUsersConnectionRepository;
import com.user.management.util.StringUtil;

import javax.validation.Validator;

@Service("userService")
public class UserServiceImpl extends BaseService implements UserService{

    /**
     * For Social API handling
     */
    private UsersConnectionRepository jpaUsersConnectionRepository;
    
    private UserRepository userRepository;

    private ApplicationConfig applicationConfig;

    public UserServiceImpl(Validator validator) {
        super(validator);
    }

    @Autowired
    public UserServiceImpl(UsersConnectionRepository usersConnectionRepository, Validator validator, ApplicationConfig applicationConfig) {
        this(validator);
        this.applicationConfig = applicationConfig;
        this.jpaUsersConnectionRepository = usersConnectionRepository;
        ((JpaUsersConnectionRepository)this.jpaUsersConnectionRepository).setUserService(this);
    }

    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Transactional
    public AuthenticatedUserToken createUser(Role role) {
        // first create a user
        User user = new User();
        user.setRole(role);
        
        // create token for that user
        AuthenticatedUserToken token = new AuthenticatedUserToken(user.getUuid().toString(),
                createAuthorizationToken(user).getToken());
        
        // save user
        userRepository.save(user);
        return token;
    }

    @Override
    public AuthorizationToken createAuthorizationToken(User user) {
        if(user.getAuthorizationToken() == null || user.getAuthorizationToken().hasExpired()) {
            user.setAuthorizationToken(new AuthorizationToken(user, applicationConfig.getAuthorizationExpiryTimeInSeconds()));
            userRepository.save(user);
        }
        return user.getAuthorizationToken();
    }

    @Override
    @Transactional
    public AuthenticatedUserToken createUser(CreateUserRequest request, Role role) {
        // validate request, make sure all the required information is good. This is done in the Rest API request.
        validate(request);
        
        // UUID is email address and UUID
        User searchedForUser = userRepository.findByEmailAddress(request.getUser().getEmailAddress());
        if (searchedForUser != null) {
            throw new DuplicateUserException();
        }

        // create user + create token
        User newUser = createNewUser(request, role);
        AuthenticatedUserToken token = new AuthenticatedUserToken(newUser.getUuid().toString(), createAuthorizationToken(newUser).getToken());
        userRepository.save(newUser);
        return token;
    }

    private User createNewUser(CreateUserRequest request, Role role) {
        User userToSave = new User(request.getUser());
        try {
            userToSave.setHashedPassword(userToSave.hashPassword(request.getPassword().getPassword()));
        }  catch (Exception e) {
            throw new AuthenticationException();
        }

        userToSave.setRole(role);
        return userToSave;
    }

    /**
     * Allow user to get their own profile or a user with administrator role to get any profile
     *
     * @param requestingUser
     * @param userIdentifier
     * @return user
     */
    @Transactional
    public ExternalUser getUser(ExternalUser requestingUser, String userIdentifier) {
        Assert.notNull(requestingUser);
        Assert.notNull(userIdentifier);

        // find the user and check if current user has the permission
        User user = ensureUserIsLoaded(userIdentifier);
        if(!requestingUser.getId().equals(user.getUuid().toString()) && !requestingUser.getRole().equalsIgnoreCase(Role.administrator.toString()))  {
           throw new AuthorizationException("User not authorized to load profile");
        }
        return new ExternalUser(user);
    }

    private User ensureUserIsLoaded(String userIdentifier) {
        User user = null;
        if (StringUtil.isValidUuid(userIdentifier)) {
            user = userRepository.findByUuid(userIdentifier);
        } else {
            user = userRepository.findByEmailAddress(userIdentifier);
        }
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }
    
    @Transactional
    public void deleteUser(ExternalUser userMakingRequest, String userId) {
        Assert.notNull(userMakingRequest);
        Assert.notNull(userId);

        User userToDelete = ensureUserIsLoaded(userId);
        if (userMakingRequest.getRole().equalsIgnoreCase(Role.administrator.toString()) && (userToDelete.hasRole(Role.anonymous) || userToDelete.hasRole(Role.authenticated))) {
            userRepository.delete(userToDelete);
        } else {
            throw new AuthorizationException("User cannot be deleted. Only users with anonymous or authenticated role can be deleted.");
        }
    }

    @Transactional
    public ExternalUser saveUser(String userId, UpdateUserRequest request) {
        validate(request);
        User user = ensureUserIsLoaded(userId);
        if(request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if(request.getEmailAddress() != null) {
            if(!request.getEmailAddress().equals(user.getEmailAddress())) {
                user.setEmailAddress(request.getEmailAddress());
                user.setVerified(false);
            }
        }
        userRepository.save(user);
        return new ExternalUser(user);
    }
    
    /**
     * {@inheritDoc}
     *
     *  Login supports authentication against an email attribute.
     *  If a User is retrieved that matches, the password in the request is hashed
     *  and compared to the persisted password for the User account.
     */
    @Transactional
    public AuthenticatedUserToken login(LoginRequest request) {
        validate(request);
        User user = null;
        user = userRepository.findByEmailAddress(request.getUsername());
        if (user == null) {
            throw new AuthenticationException();
        }
        String hashedPassword = null;
        try {
            hashedPassword = user.hashPassword(request.getPassword());
        } catch (Exception e) {
            throw new AuthenticationException();
        }
        if (hashedPassword.equals(user.getHashedPassword())) {
            return new AuthenticatedUserToken(user.getUuid().toString(), createAuthorizationToken(user).getToken());
        } else {
            throw new AuthenticationException();
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * Associate a Connection with a User account. If one does not exist a new User is created and linked to the
     * {@link com.porterhead.rest.user.domain.SocialUser} represented in the Connection details.
     *
     * <P></P>
     *
     * A AuthorizationToken is generated and any Profile data that can be collected from the Social account is propagated to the User object.
     *
     */
    @Transactional
    public AuthenticatedUserToken socialLogin(Connection<?> connection) {

        List<String> userUuids = jpaUsersConnectionRepository.findUserIdsWithConnection(connection);
        if(userUuids.size() == 0) {
            throw new AuthenticationException();
        }
        User user = userRepository.findByUuid(userUuids.get(0)); //take the first one if there are multiple userIds for this provider Connection
        if (user == null) {
            throw new AuthenticationException();
        }
        updateUserFromProfile(connection, user);
        return new AuthenticatedUserToken(user.getUuid().toString(), createAuthorizationToken(user).getToken());
    }
    
    private void updateUserFromProfile(Connection<?> connection, User user) {
        UserProfile profile = connection.fetchUserProfile();
        user.setEmailAddress(profile.getEmail());
        user.setFirstName(profile.getFirstName());
        user.setLastName(profile.getLastName());
        //users logging in from social network are already verified
        user.setVerified(true);
        if(user.hasRole(Role.anonymous)) {
            user.setRole(Role.authenticated);
        }
        userRepository.save(user);
    }
}
