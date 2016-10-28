package com.kameo.challenger.utils.rest.annotations;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface WebResponseStatus {
    int OK=200; // OK - Everything is working
    int CREATED = 201; // OK - New resource has been created
    int SUCCESSFULLY_DELETED=204; // OK - The resource was successfully deleted

    int SERVICE_UAVAILABLE=503;// timeout on async
    int value();
}