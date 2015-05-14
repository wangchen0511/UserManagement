package com.user.management.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.user.management.authorization.AuthorizationRequestContext;
import com.user.management.authorization.AuthorizationService;
import com.user.management.authorization.impl.RequestSigningAuthorizationService;
import com.user.management.authorization.impl.SecurityContextImpl;
import com.user.management.authorization.impl.SessionTokenAuthorizationService;
import com.user.management.config.ApplicationConfig;
import com.user.management.repositories.UserRepository;
import com.user.management.rest.api.ExternalUser;
import com.user.management.services.UserService;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * A Servlet filter class for authorizing requests.
 *
 *
 * The role of this filter class is to set a {@link javax.ws.rs.core.SecurityContext} in the {@link com.sun.jersey.spi.container.ContainerRequest}
 *
 * @see {@link com.porterhead.rest.authorization.impl.SecurityContextImpl}
 *
 * @author: Iain Porter
 */
@Component
@Provider
public class SecurityContextFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityContextFilter.class);

    protected static final String HEADER_AUTHORIZATION = "Authorization";

    protected static final String HEADER_DATE = "x-java-rest-date";

    protected static final String HEADER_NONCE = "nonce";

    private AuthorizationService authorizationService;

    ApplicationConfig config;

    @Autowired
    public SecurityContextFilter(UserRepository userRepository, UserService userService, ApplicationConfig config) {
        delegateAuthorizationService(userRepository, userService, config);
        this.config = config;

    }

    /**
     * Specify the AuthorizationService that the application should use
     *
     * @param userRepository
     * @param userService
     * @param config
     */
    private void delegateAuthorizationService(UserRepository userRepository, UserService userService, ApplicationConfig config) {
        if(config.requireSignedRequests()) {
            this.authorizationService = new RequestSigningAuthorizationService(userRepository, userService, config);
        } else {
            this.authorizationService = new SessionTokenAuthorizationService(userRepository);
        }
    }


    public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    public ContainerResponseFilter getResponseFilter() {
        return null;
    }

    @Autowired
    public void setConfig(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * If there is an Authorisation header in the request extract the session token and retrieve the user
     *
     * Delegate to the AuthorizationService to validate the request
     *
     * If the request has a valid session token and the user is validated then a user object will be added to the security context
     *
     * Any Resource Controllers can assume the user has been validated and can merely authorize based on the role
     *
     * Resources with @PermitAll annotation do not require an Authorization header but will still be filtered
     *
     * @param request the ContainerRequest to filter
     *
     * @return the ContainerRequest with a SecurityContext added
     * @throws UnsupportedEncodingException 
     * @throws NoSuchAlgorithmException 
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authToken = requestContext.getHeaderString(HEADER_AUTHORIZATION);
        String requestDateString = requestContext.getHeaderString(HEADER_DATE);
        String nonce = requestContext.getHeaderString(HEADER_NONCE);
        AuthorizationRequestContext context = new AuthorizationRequestContext(requestContext.getUriInfo().getPath(), requestContext.getMethod(),
                            requestDateString, nonce, authToken);
        ExternalUser externalUser;
        try {
            externalUser = authorizationService.authorize(context);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }

        requestContext.setSecurityContext(new SecurityContextImpl(externalUser));
    }

}
