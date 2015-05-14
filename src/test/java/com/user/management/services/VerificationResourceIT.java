package com.user.management.services;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.user.management.domain.User;
import com.user.management.mail.MockJavaMailSender;
import com.user.management.repositories.UserRepository;
import com.user.management.resource.PasswordResource;
import com.user.management.resource.UserResource;
import com.user.management.resource.VerificationResource;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.EmailVerificationRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.LoginRequest;
import com.user.management.rest.api.LostPasswordRequest;
import com.user.management.rest.api.PasswordRequest;
import com.user.management.util.ObjectMapperProvider;

public class VerificationResourceIT extends JerseyTestNg.ContainerPerClassTest {

    private static Pattern pattern = Pattern.compile(".*validate.html\\?(.+)\">Verify.*", Pattern.DOTALL);
    
    protected UserService userService;
    protected UserRepository resp;
    
    
    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    
    @Override
    protected DeploymentContext configureDeployment() {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        
        ResourceConfig resConfig = new ResourceConfig(VerificationResource.class)
                                        .register(UserResource.class)
                                        .register(org.glassfish.jersey.jackson.JacksonFeature.class)
                                        .register(ObjectMapperProvider.class)
                                        .register(RequestContextFilter.class);
        
        return ServletDeploymentContext.forServlet(new ServletContainer(resConfig))
                        .contextParam("contextConfigLocation", "classpath:test-root-context.xml")
                        .contextParam("spring.profiles.default", "dev")
                        .addListener(ContextLoaderListener.class)
                        .build();
    }

    String oldPassword = "131312313123131";
    String newPassword = "913123adadad12313";
    String email = "murong@gmail.com";
    String firstName = "murong";
    String lastName = "lang";
    
    @Test
    public void setUpServices() {
        resp = ApplicationContextProvider.getApplicationContext().getBean(UserRepository.class);
    }
    
    @Test(description="signup test", dependsOnMethods="setUpServices")
    private void signupUser() throws MessagingException, InterruptedException {
        ExternalUser externalUser = new ExternalUser();
        externalUser.setEmailAddress(email);
        externalUser.setFirstName(firstName);
        externalUser.setLastName(lastName);
        
        PasswordRequest passwordRequest = new PasswordRequest(oldPassword);
        CreateUserRequest createUserRequest = new CreateUserRequest(externalUser, passwordRequest);
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
    
    @Test(description="verification token", dependsOnMethods="signupUser")
    private void lostPassword() throws Exception {
        EmailVerificationRequest emailReq = new EmailVerificationRequest(email);
        
        final Response response = target("verify/tokens").request().post(Entity.<EmailVerificationRequest>entity(emailReq, MediaType.APPLICATION_JSON));

        Assert.assertEquals(200, response.getStatus());
       
        Thread.sleep(1000);
        List<MimeMessage> messages = MockJavaMailSender.messages;
        Assert.assertEquals(messages.size(), 1);
        MimeMessage message = messages.get(0);
        Assert.assertEquals(message.getAllRecipients()[0].toString(), email);

        Multipart multipart = (Multipart)message.getContent();
        String content = (String)multipart.getBodyPart(0).getContent();
        
        System.out.println(content);
        
        Matcher mat = pattern.matcher(content);
        Assert.assertTrue(mat.matches());
        
        String tokenStr = mat.group(1);
        System.out.println(tokenStr);
        
        User user = resp.findByEmailAddress(email);
        Assert.assertFalse(user.isVerified());
        
        final Response response1 = target("verify/tokens/" + tokenStr).request().post(null);

        Assert.assertEquals(200, response1.getStatus());

        user = resp.findByEmailAddress(email);
        Assert.assertTrue(user.isVerified());
    }
    
}
