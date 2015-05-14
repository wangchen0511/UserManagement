package com.user.management.rest.api;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author: Iain Porter
 */
@XmlRootElement
public class LoginRequest {

    // ths is emailaddress in this case
    @NotNull
    private String username;

    @Length(min=8, max=30)
    @NotNull
    private String password;

    public LoginRequest(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
