package com.kameo.challenger.odb;

import com.challenger.eviauth.odb.api.IIdentity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
@Entity
public @Data class UserODB implements IIdentity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    String firstName;
    String lastName;
    String login;
    String passwordHash;
    String salt;
}
