package com.user.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.user.management.domain.VerificationToken;

/**
 * @version 1.0
 * @author: Iain Porter iain.porter@porterhead.com
 * @since 14/09/2012
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query("select t from VerificationToken t where uuid = ?")
    VerificationToken findByUuid(String uuid);

    @Query("select t from VerificationToken t where token = ?")
    VerificationToken findByToken(String token);
}
