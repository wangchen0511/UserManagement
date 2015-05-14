package com.user.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.util.MultiValueMap;

import com.user.management.domain.SocialUser;
import com.user.management.domain.User;

import java.util.List;
import java.util.Set;

/**
 * User: porter
 * Date: 15/05/2012
 * Time: 16:35
 */

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {

    List<SocialUser> findAllByUser(User user);

    List<SocialUser> findByUserAndProviderId(User user, String providerId);

    List<SocialUser> findByProviderIdAndProviderUserId(String providerId, String providerUserId);

    //TODO will need a JPA Query here
    List<SocialUser> findByUserAndProviderUserId(User user, MultiValueMap<String, String> providerUserIds);

    @Query("Select userId from SocialUser userId where providerId = ? AND providerUserId in (?)")
    Set<String> findByProviderIdAndProviderUserId(String providerId, Set<String> providerUserIds);

    SocialUser findByUserAndProviderIdAndProviderUserId(User user, String providerId, String providerUserId);

}
