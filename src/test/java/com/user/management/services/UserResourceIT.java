package com.user.management.services;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ContextLoaderListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.user.management.domain.User;
import com.user.management.filter.ResourceFilterFactory;
import com.user.management.filter.SecurityContextFilter;
import com.user.management.mail.MockJavaMailSender;
import com.user.management.mail.impl.MailSenderServiceImpl;
import com.user.management.repositories.UserRepository;
import com.user.management.resource.UserResource;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.LoginRequest;
import com.user.management.rest.api.PasswordRequest;
import com.user.management.rest.api.TestResult;
import com.user.management.rest.api.UpdateUserRequest;
import com.user.management.util.ObjectMapperProvider;

public class UserResourceIT extends JerseyTestNg.ContainerPerClassTest {
    
    protected UserService userService;
    protected UserRepository resp;
    private String uid;
    
    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    
    @Override
    protected DeploymentContext configureDeployment() {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        
        ResourceConfig resConfig = new ResourceConfig(UserResource.class)
                                        .register(org.glassfish.jersey.jackson.JacksonFeature.class)
                                        .register(ObjectMapperProvider.class)
                                        .register(RequestContextFilter.class)
                                        .register(ResourceFilterFactory.class)
                                        .register(RolesAllowedDynamicFeature.class);

        return ServletDeploymentContext.forServlet(new ServletContainer(resConfig))
                        .contextParam("contextConfigLocation", "classpath:test-root-context.xml")
                        .contextParam("spring.profiles.default", "dev")
                        .addListener(ContextLoaderListener.class)
                        .build();
    }

    String password = "131312313123131";
    String email = "murong@gmail.com";
    String firstName = "murong";
    String lastName = "lang";
    
    @Test
    public void setUpServices() {
        resp = ApplicationContextProvider.getApplicationContext().getBean(UserRepository.class);
    }
    
    @Test(description="signup test", dependsOnMethods="setUpServices")
    private void signupUser() throws MessagingException, InterruptedException, JsonProcessingException {
        MockJavaMailSender.messages.clear();
        
        ExternalUser externalUser = new ExternalUser();
        externalUser.setEmailAddress(email);
        externalUser.setFirstName(firstName);
        externalUser.setLastName(lastName);
        
        PasswordRequest passwordRequest = new PasswordRequest(password);
        CreateUserRequest createUserRequest = new CreateUserRequest(externalUser, passwordRequest);
        
        String json = new ObjectMapperProvider().getContext(null).writeValueAsString(createUserRequest);
        System.out.println(json);
        
        
        UpdateUserRequest updateUser = new UpdateUserRequest();
        updateUser.setEmailAddress("wangchen0511@yahoo.com");
        updateUser.setFirstName("Chen");
        updateUser.setLastName("Wang");
        
        String json1 = new ObjectMapperProvider().getContext(null).writeValueAsString(updateUser);
        System.out.println(json1);
        
        final Response response = target("user").request().post(Entity.<CreateUserRequest>entity(createUserRequest, MediaType.APPLICATION_JSON));
        
        AuthenticatedUserToken token = response.readEntity(AuthenticatedUserToken.class);
        
        Assert.assertEquals(201, response.getStatus());
       
        User user = resp.findByEmailAddress(externalUser.getEmailAddress());
        
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getFirstName(), firstName);
        Assert.assertEquals(user.getAuthorizationToken().getToken(), token.getToken());
        Assert.assertEquals(user.getAuthorizationToken().getUser().getUuid().toString(), token.getUserId());

        Thread.sleep(1000);
        List<MimeMessage> messages = MockJavaMailSender.messages;
        Assert.assertEquals(messages.size(), 1);
        MimeMessage message = messages.get(0);
        Assert.assertEquals(message.getAllRecipients()[0].toString(), email);
        messages.clear();
    }
    
    @Test(description="login test", dependsOnMethods="signupUser")
    private void loginUser() throws MessagingException, InterruptedException {
        LoginRequest login = new LoginRequest();
        login.setPassword(password);
        login.setUsername(email);
        
        final Response response = target("user/login").request().post(Entity.<LoginRequest>entity(login, MediaType.APPLICATION_JSON));
        
        AuthenticatedUserToken token = response.readEntity(AuthenticatedUserToken.class);
        
        Assert.assertEquals(200, response.getStatus());
       
        User user = resp.findByEmailAddress(email);
        
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getFirstName(), firstName);
        Assert.assertEquals(user.getAuthorizationToken().getToken(), token.getToken());
        Assert.assertEquals(user.getAuthorizationToken().getUser().getUuid().toString(), token.getUserId());
        this.uid = user.getUuid().toString();
    }
    
    @Test(description="get user permission deny", dependsOnMethods="loginUser")
    public void permissionDenyGetUser() {
        User user = resp.findByEmailAddress(email);
        final Response response = target("user/" + user.getUuid().toString()).request().get();
        
        Assert.assertEquals(response.getStatus(), 401);
    }
    
    @Test(description="get user permission allowed", dependsOnMethods="loginUser")
    public void permissionAllowedGetUser() {
        LoginRequest login = new LoginRequest();
        login.setPassword(password);
        login.setUsername(email);
        
        final Response response = target("user/login").request().post(Entity.<LoginRequest>entity(login, MediaType.APPLICATION_JSON));
        
        AuthenticatedUserToken token = response.readEntity(AuthenticatedUserToken.class);
        
        Assert.assertEquals(200, response.getStatus());
        
        User user = resp.findByEmailAddress(email);
        Response response1 = target("user/" + user.getUuid().toString()).request().header("Authorization", token.getToken()).get();
        
        Assert.assertEquals(response.getStatus(), 200);
        
        ExternalUser externalUser = response1.readEntity(ExternalUser.class);
        Assert.assertEquals(email, externalUser.getEmailAddress());
    }
}