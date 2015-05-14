package com.user.management.services;


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import com.user.management.config.ApplicationConfig;
import com.user.management.domain.Role;
import com.user.management.domain.User;
import com.user.management.exceptions.AuthorizationException;
import com.user.management.repositories.UserRepository;
import com.user.management.rest.api.AuthenticatedUserToken;
import com.user.management.rest.api.CreateUserRequest;
import com.user.management.rest.api.ExternalUser;
import com.user.management.rest.api.LoginRequest;
import com.user.management.rest.api.PasswordRequest;
import com.user.management.rest.api.UpdateUserRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.UnableToRegisterMBeanException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * @author Iain Porter
 */
@Test
@ContextConfiguration("classpath:test-root-context.xml")
@ActiveProfiles(profiles = "dev")
@Transactional
@TransactionConfiguration(defaultRollback=true)
public class UserServiceTest  extends AbstractTestNGSpringContextTests {

    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ApplicationConfig applicationConfig;

    private List<AuthenticatedUserToken> normalUserTokens = new ArrayList<>();
    private List<AuthenticatedUserToken> adminUserTokens = new ArrayList<>();
    
    @DataProvider
    public Object[][] userInformation() {
        return new Object[][]{
            { "wangchen0511@yahoo.com", "Chen", "Wang", "12313144", Role.authenticated  },
            { "wangchen0511@gmail.com", "Murong", "Lang", "1231313131", Role.authenticated },
            { "murong@gmail.com", "mimi", "xiong", "dadaddadad", Role.administrator  }
        };
    }
    
    @Test(dataProvider = "userInformation")
    public void createGivenUser(String emailAddress, String firstName, String lastName, String password, Role role) throws Exception {
        ExternalUser externalUser = new ExternalUser();
        externalUser.setEmailAddress(emailAddress);
        externalUser.setFirstName(firstName);
        externalUser.setLastName(lastName);
        
        PasswordRequest passwordRequest = new PasswordRequest(password);
        
        CreateUserRequest createUserRequest = new CreateUserRequest(externalUser, passwordRequest);
        
        AuthenticatedUserToken userToken = userService.createUser(createUserRequest, role);
        User user = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getFirstName(), firstName);
        Assert.assertEquals(user.getEmailAddress(), emailAddress);
        Assert.assertEquals(user.getLastName(), lastName);
        Assert.assertEquals(user.getHashedPassword(), user.hashPassword(password));
        
        if(role.equals(Role.administrator)) {
            adminUserTokens.add(userToken);
        } else if (role.equals(Role.authenticated)) {
            normalUserTokens.add(userToken);
        }
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void createDefaultUser() throws Exception {
        AuthenticatedUserToken userToken = userService.createUser(Role.authenticated);
        User user = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getRole(), Role.authenticated);
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void createInvalidUser() {
        String emailAddress = "wangchen0511yahoo.com";
        String firstName = "Chen";
        String lastName = "Wang";
        String password = "12345";
        
        ExternalUser externalUser = new ExternalUser();
        externalUser.setEmailAddress(emailAddress);
        externalUser.setFirstName(firstName);
        externalUser.setLastName(lastName);
        
        PasswordRequest passwordRequest = new PasswordRequest(password);
        
        CreateUserRequest createUserRequest = new CreateUserRequest(externalUser, passwordRequest);
                
        try{
            AuthenticatedUserToken userToken = userService.createUser(createUserRequest, Role.authenticated);
            Assert.fail();
        } catch (WebApplicationException e) {
            System.out.println(e.getMessage());
        }
        
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void getSameUser() {
        AuthenticatedUserToken userToken = normalUserTokens.get(0);
        User user = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(user);
        
        ExternalUser returnedUser = userService.getUser(new ExternalUser(user), user.getUuid().toString());
        
        Assert.assertNotNull(returnedUser);
        Assert.assertEquals(returnedUser.getFirstName(), user.getFirstName());
        Assert.assertEquals(returnedUser.getLastName(), user.getLastName());
        Assert.assertEquals(returnedUser.getEmailAddress(), user.getEmailAddress());
        Assert.assertEquals(returnedUser.getId(), user.getUuid().toString());
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void admimGetDifferentUser() {
        // normal user        
        AuthenticatedUserToken userToken = normalUserTokens.get(0);
        User normalUser = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(normalUser);
        
        // admin
        AuthenticatedUserToken adminUserToken = adminUserTokens.get(0);
        User adminUser = userRepository.findBySession(adminUserToken.getToken());
        Assert.assertNotNull(adminUser);
        
        ExternalUser returnedUser = userService.getUser(new ExternalUser(adminUser), normalUser.getUuid().toString());
        
        Assert.assertNotNull(returnedUser);
        Assert.assertEquals(returnedUser.getFirstName(), normalUser.getFirstName());
        Assert.assertEquals(returnedUser.getLastName(), normalUser.getLastName());
        Assert.assertEquals(returnedUser.getEmailAddress(), normalUser.getEmailAddress());
        Assert.assertEquals(returnedUser.getId(), normalUser.getUuid().toString());
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void normalUserGetDifferrntUser() {
        // normal user
        AuthenticatedUserToken userToken = normalUserTokens.get(0);
        User normalUser = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(normalUser);
        
        // second normal user
        AuthenticatedUserToken userToken2 = normalUserTokens.get(1);
        User normalUser2 = userRepository.findBySession(userToken2.getToken());
        Assert.assertNotNull(normalUser);
        
        try {
            ExternalUser returnedUser = userService.getUser(new ExternalUser(normalUser), normalUser2.getUuid().toString());
            Assert.fail();
        } catch (AuthorizationException e) {
        }
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void normalUserCanNotDeleteOtherUsers() {
        // normal user c
        AuthenticatedUserToken userToken = normalUserTokens.get(0);
        User normalUser = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(normalUser);
        
        // second normal user
        AuthenticatedUserToken userToken2 = normalUserTokens.get(1);
        User normalUser2 = userRepository.findBySession(userToken2.getToken());
        Assert.assertNotNull(normalUser);
        
        try {
            userService.deleteUser(new ExternalUser(normalUser), normalUser2.getUuid().toString());
            Assert.fail();
        } catch (AuthorizationException e) {
        }
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void normalUserCanNotDeleteThemselves() throws Exception {
        // normal user c
        AuthenticatedUserToken userToken = normalUserTokens.get(0);
        User normalUser = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(normalUser);
        
        try {
            userService.deleteUser(new ExternalUser(normalUser), normalUser.getUuid().toString());
            Assert.fail();
        } catch (AuthorizationException e) {
        }
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void adminUserCanDeleteNormal() throws Exception {
        // admin user c
        AuthenticatedUserToken userToken = adminUserTokens.get(0);
        User adminUser = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(adminUser);
        
        // normal user c
        AuthenticatedUserToken normalUserToken = normalUserTokens.get(0);
        User normalUser = userRepository.findBySession(normalUserToken.getToken());
        Assert.assertNotNull(normalUser);
        
        try {
            userService.deleteUser(new ExternalUser(adminUser), normalUser.getUuid().toString());
            normalUserTokens.remove(0);
        } catch (AuthorizationException e) {
            Assert.fail();
        }
        
        normalUser = userRepository.findBySession(normalUserToken.getToken());
        Assert.assertNull(normalUser);
        
        // add back the deleted user for further tests
        Object[] infor = userInformation()[0];
        createGivenUser((String) infor[0], (String) infor[1], (String) infor[2], (String) infor[3], (Role) infor[4]);
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void normalUserCanUpdateThemselves() throws Exception {
        // normal user c
        AuthenticatedUserToken userToken = normalUserTokens.get(0);
        User normalUser = userRepository.findBySession(userToken.getToken());
        Assert.assertNotNull(normalUser);
        
        // new email first name last name
        String email = "zhuzhu@gmail.com";
        String first = "zhu";
        String last = "bao";
        
        UpdateUserRequest newInfor = new UpdateUserRequest();
        newInfor.setEmailAddress(email);
        newInfor.setFirstName(first);
        newInfor.setLastName(last);
        
        try {
            ExternalUser newExternalUser = userService.saveUser(normalUser.getUuid().toString(), newInfor);
            Assert.assertEquals(newExternalUser.getEmailAddress(), email);
            Assert.assertEquals(newExternalUser.getFirstName(), first);
            Assert.assertEquals(newExternalUser.getLastName(), last);
        } catch (AuthorizationException e) {
            Assert.fail();
        }
    }
    
    @Test(dependsOnMethods="createGivenUser")
    public void userLogin() throws Exception {
        // normal user c
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("12313144");
        loginRequest.setUsername("wangchen0511@yahoo.com");
        AuthenticatedUserToken token = userService.login(loginRequest);
        
        User user = userRepository.findBySession(token.getToken());
        
        Assert.assertEquals(user.getEmailAddress(), "wangchen0511@yahoo.com");
        
    }
}
