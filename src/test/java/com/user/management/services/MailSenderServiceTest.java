package com.user.management.services;


import org.apache.commons.lang.RandomStringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.user.management.config.ApplicationConfig;
import com.user.management.domain.Role;
import com.user.management.domain.User;
import com.user.management.domain.VerificationToken;
import com.user.management.gateway.EmailServicesGateway;
import com.user.management.mail.EmailServiceTokenModel;
import com.user.management.mail.MailSenderService;
import com.user.management.mail.MockJavaMailSender;
import com.user.management.mail.impl.MailSenderServiceImpl;
import com.user.management.repositories.UserRepository;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.PasswordRequest;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.Base64;
import java.util.List;


/**
 * @author: Iain Porter
 */
@Test
@ContextConfiguration("classpath:test-root-context.xml")
@ActiveProfiles(profiles = "dev")
@Transactional
@TransactionConfiguration(defaultRollback=true)
public class MailSenderServiceTest extends AbstractTestNGSpringContextTests {

    private MailSenderService mailService;

    private MockJavaMailSender mailSender;

    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;
     
    @Autowired
    VelocityEngine velocityEngine;

    @Autowired
    ApplicationConfig config;
    
    @Autowired
    EmailServicesGateway emailGateWay;

    @BeforeClass
    public void setUpServices() {
        mailSender = new MockJavaMailSender();
        mailService = new MailSenderServiceImpl(mailSender, velocityEngine);
        ((MailSenderServiceImpl)mailService).setConfig(config);
    }


    @Test
    public void sendVerificationEmail() throws Exception {
        AuthenticatedUserToken userToken = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(userToken.getUserId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.emailVerification, 120);
        mailService.sendVerificationEmail(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        assertOnMailResult(user, token);
    }
    

    @Test
    public void sendVerificationEmailByGateway() throws Exception {
        MockJavaMailSender.messages.clear();
        
        AuthenticatedUserToken userToken = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(userToken.getUserId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.emailVerification, 120);
        emailGateWay.sendVerificationToken(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        // wait the message is taken out of the queue and sent
        Thread.sleep(1000);
        assertOnMailResult(user, token);
    }

    @Test
    public void sendRegistrationEmail() throws Exception {
        MockJavaMailSender.messages.clear();
        
        AuthenticatedUserToken userToken = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(userToken.getUserId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.emailRegistration, 120);
        mailService.sendRegistrationEmail(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        assertOnMailResult(user, token);
    }
    
    @Test
    public void sendRegistrationEmailByGateway() throws Exception {
        MockJavaMailSender.messages.clear();
        
        AuthenticatedUserToken userToken = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(userToken.getUserId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.emailRegistration, 120);
        emailGateWay.sendVerificationToken(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        Thread.sleep(1000);
        assertOnMailResult(user, token);
    }

    @Test
    public void sendLostPasswordEmail() throws Exception {
        MockJavaMailSender.messages.clear();

        AuthenticatedUserToken userToken = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(userToken.getUserId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.lostPassword, 120);
        mailService.sendLostPasswordEmail(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        assertOnMailResult(user, token);
    }
    
    @Test
    public void sendLostPasswordEmailByGateway() throws Exception {
        MockJavaMailSender.messages.clear();

        AuthenticatedUserToken userToken = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(userToken.getUserId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.lostPassword, 120);
        emailGateWay.sendVerificationToken(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        Thread.sleep(1000);
        assertOnMailResult(user, token);
    }

    private void assertOnMailResult(User user, VerificationToken token) throws MessagingException, IOException {
        List<MimeMessage> messages = mailSender.getMessages();
        Assert.assertEquals(messages.size(), 1);
        MimeMessage message = messages.get(0);
        Assert.assertEquals(message.getAllRecipients()[0].toString(), user.getEmailAddress());
        
        Multipart multipart = (Multipart)message.getContent();
        String content = (String)multipart.getBodyPart(0).getContent();
        Assert.assertTrue(content.contains(new String(Base64.getEncoder().encode(token.getToken().getBytes()))));
        mailSender.getMessages().clear();
    }

    protected AuthenticatedUserToken createUserWithRandomUserName(Role role) {
        CreateUserRequest request = getDefaultCreateUserRequest();
        return userService.createUser(request, role);
    }

    protected CreateUserRequest getDefaultCreateUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUser(getUser());
        request.setPassword(new PasswordRequest("password"));
        return request;
    }

    protected ExternalUser getUser() {
        ExternalUser user = ExternalUserBuilder.create().withFirstName("John")
                .withLastName("Smith")
                .withEmailAddress(createRandomEmailAddress())
                .build();
        return user;
    }

    protected String createRandomEmailAddress() {
        return RandomStringUtils.randomAlphabetic(8) + "@example.com";
    }
}
