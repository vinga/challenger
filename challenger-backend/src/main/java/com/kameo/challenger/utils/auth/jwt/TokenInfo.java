package com.kameo.challenger.utils.auth.jwt;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class TokenInfo {
    private DateTime issued;
    private DateTime expires;
}
