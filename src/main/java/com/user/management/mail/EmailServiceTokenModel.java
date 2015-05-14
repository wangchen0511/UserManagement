package com.user.management.mail;

import com.user.management.domain.User;
import com.user.management.domain.VerificationToken;

import java.io.Serializable;
import java.util.Base64;

public class EmailServiceTokenModel implements Serializable {

    private final String emailAddress;
    private final String token;
    private final VerificationToken.VerificationTokenType tokenType;
    private final String hostNameUrl;


    public EmailServiceTokenModel(User user, VerificationToken token, String hostNameUrl)  {
        this.emailAddress = user.getEmailAddress();
        this.token = token.getToken();
        this.tokenType = token.getTokenType();
        this.hostNameUrl = hostNameUrl;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEncodedToken() {
        return Base64.getEncoder().encodeToString(token.getBytes());
    }

    public String getToken() {
        return token;
    }

    public VerificationToken.VerificationTokenType getTokenType() {
        return tokenType;
    }

    public String getHostNameUrl() {
        return hostNameUrl;
    }
}
