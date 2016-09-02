package com.kameo.challenger.utils.auth.jwt;

/**
 * Created by kmyczkowska on 2016-09-01.
 */

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class TokenInfo {
    private DateTime issued;
    private DateTime expires;
}
