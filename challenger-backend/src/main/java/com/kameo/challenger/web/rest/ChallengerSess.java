package com.kameo.challenger.web.rest;

import com.kameo.challenger.utils.auth.jwt.TokenInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class ChallengerSess extends TokenInfo {
    long userId;


    public long getUserId() {
        return userId;
    }


    public void setUserId(long userId) {
        this.userId = userId;
    }
}
