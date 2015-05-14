package com.user.management.services;

import com.user.management.domain.VerificationToken;
import com.user.management.rest.api.LostPasswordRequest;
import com.user.management.rest.api.PasswordRequest;


/**
 *
 * @version 1.0
 * @author: Iain Porter iain.porter@porterhead.com
 * @since 10/09/2012
 */
public interface VerificationTokenService {

    public VerificationToken sendEmailVerificationToken(String userId);

    public VerificationToken sendEmailRegistrationToken(String userId);

    public VerificationToken sendLostPasswordToken(LostPasswordRequest lostPasswordRequest);

    public VerificationToken verify(String base64EncodedToken);

    public VerificationToken generateEmailVerificationToken(String emailAddress);

    public VerificationToken resetPassword(String base64EncodedToken, PasswordRequest passwordRequest);
}
