package com.user.management.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.user.management.config.ApplicationConfig;
import com.user.management.domain.Role;
import com.user.management.exceptions.AuthorizationException;
import com.user.management.gateway.EmailServicesGateway;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.LoginRequest;
import com.user.management.rest.api.OAuth2Request;
import com.user.management.rest.api.PasswordRequest;
import com.user.management.rest.api.TestResult;
import com.user.management.rest.api.UpdateUserRequest;
import com.user.management.services.UserService;
import com.user.management.services.VerificationTokenService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.net.URI;

/**
 * User: porter
 * Date: 12/03/2012
 * Time: 18:57
 */
@Path("/user")
@Component
//@Produces({MediaType.APPLICATION_JSON})
//@Consumes({MediaType.APPLICATION_JSON})
public class UserResource {

    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    protected UserService userService;

    @Autowired
    protected VerificationTokenService verificationTokenService;

    @Autowired
    protected EmailServicesGateway emailServicesGateway;

    @Context
    protected UriInfo uriInfo;

    @Autowired
    ApplicationConfig config;

    @Autowired
    public UserResource(ConnectionFactoryLocator connectionFactoryLocator) {
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

//    @PermitAll
//    @GET
//    @Path("test")
//    public Response testOnly() {
//        return Response.ok().entity("hello world").build();
//    }
    
    @PermitAll
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signupUser(CreateUserRequest request) {
        AuthenticatedUserToken token = userService.createUser(request, Role.authenticated);
        verificationTokenService.sendEmailRegistrationToken(token.getUserId());
        URI location = uriInfo.getAbsolutePathBuilder().path(token.getUserId()).build();
        return Response.created(location).entity(token).build();
    }

    @RolesAllowed("admin")
    @Path("{userId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@Context SecurityContext sc, @PathParam("userId") String userId) {
        ExternalUser userMakingRequest = (ExternalUser)sc.getUserPrincipal();
        userService.deleteUser(userMakingRequest, userId);
        return Response.ok().build();
    }

    @PermitAll
    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        AuthenticatedUserToken token = userService.login(request);
        return getLoginResponse(token);
    }

    @PermitAll
    @Path("login/{providerId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response socialLogin(@PathParam("providerId") String providerId, OAuth2Request request) {
        // get connection
        OAuth2ConnectionFactory<?> connectionFactory = (OAuth2ConnectionFactory<?>) connectionFactoryLocator.getConnectionFactory(providerId);
        Connection<?> connection = connectionFactory.createConnection(new AccessGrant(request.getAccessToken()));
        AuthenticatedUserToken token = userService.socialLogin(connection);
        return getLoginResponse(token);
    }

    @RolesAllowed({"authenticated"})
    @Path("{userId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context SecurityContext sc, @PathParam("userId") String userId) {
        ExternalUser userMakingRequest = (ExternalUser)sc.getUserPrincipal();
        ExternalUser user =  userService.getUser(userMakingRequest, userId);
        return Response.ok().entity(user).build();
    }

    @RolesAllowed({"authenticated"})
    @Path("{userId}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@Context SecurityContext sc, @PathParam("userId") String userId, UpdateUserRequest request) {
        ExternalUser userMakingRequest = (ExternalUser)sc.getUserPrincipal();
        if(!userMakingRequest.getId().equals(userId)) {
            throw new AuthorizationException("User not authorized to modify this profile");
        }
        boolean sendVerificationToken = StringUtils.hasLength(request.getEmailAddress()) &&
                !request.getEmailAddress().equals(userMakingRequest.getEmailAddress());
        ExternalUser savedUser = userService.saveUser(userId, request);
        if(sendVerificationToken) {
            verificationTokenService.sendEmailVerificationToken(savedUser.getId());
        }
        return Response.ok().build();
    }

    private Response getLoginResponse(AuthenticatedUserToken token) {
        URI location = UriBuilder.fromPath(uriInfo.getBaseUri() + "user/" + token.getUserId()).build();
        return Response.ok().entity(token).contentLocation(location).build();
    }

}
