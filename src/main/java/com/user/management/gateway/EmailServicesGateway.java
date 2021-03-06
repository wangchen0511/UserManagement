package com.user.management.gateway;

import com.user.management.mail.EmailServiceTokenModel;


/**
 *
 * @version 1.0
 * @author: Iain Porter iain.porter@porterhead.com
 * @since 11/09/2012
 */
public interface EmailServicesGateway {

    public void sendVerificationToken(EmailServiceTokenModel model);

}
