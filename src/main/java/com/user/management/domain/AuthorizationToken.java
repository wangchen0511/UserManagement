package com.user.management.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="rest_authorization_token")
public class AuthorizationToken extends AbstractPersistable<Long> {

    /**
     * 
     */
    private static final long serialVersionUID = -7101100260675830223L;

    private final static int DEFAULT_TIME_TO_LIVE_IN_SECONDS = 60 * 60 * 24 * 30; //30 Days

    @Column(length=36)
    private String token;

    private LocalDateTime timeCreated;

    private LocalDateTime expirationDate;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    public AuthorizationToken() {}

    public AuthorizationToken(User user) {
        this(user, DEFAULT_TIME_TO_LIVE_IN_SECONDS);
    }

    public AuthorizationToken(User user, int timeToLiveInSeconds) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.timeCreated = LocalDateTime.now();
        this.expirationDate = this.timeCreated.plusSeconds(DEFAULT_TIME_TO_LIVE_IN_SECONDS);
    }

    public boolean hasExpired() {
        LocalDateTime now = LocalDateTime.now();
        return this.expirationDate != null && this.expirationDate.isBefore(now);
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }
}
