package com.user.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.user.management.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository  extends JpaRepository<User, Long> {

    User findByEmailAddress(String emailAddress);

    @Query("select u from User u where uuid = ?")
    User findByUuid(String uuid);

    @Query("select u from User u where u in (select user from AuthorizationToken where lastUpdated < ?)")
    List<User> findByExpiredSession(LocalDateTime lastUpdated);

    @Query("select u from User u where u = (select user from AuthorizationToken where token = ?)")
    User findBySession(String token);

}
