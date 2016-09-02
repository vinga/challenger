package com.kameo.challenger.web.rest;

import com.kameo.challenger.utils.auth.jwt.TokenInfo;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Created by kmyczkowska on 2016-09-01.
 */
@RequestScope
@Component
@Data
public class ChallengerSess extends TokenInfo {
    long userId;
}
