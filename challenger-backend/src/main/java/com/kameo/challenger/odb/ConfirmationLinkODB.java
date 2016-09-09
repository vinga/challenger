package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

/**
 * Created by kmyczkowska on 2016-09-02.
 */
@Entity
@Data
@ToString(of= IIdentity.id_column)
@NoArgsConstructor

public class ConfirmationLinkODB implements IIdentity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    String uid;

    @Enumerated
    ConfirmationLinkType confirmationLinkType;

    String fieldLogin;
    String fieldPasswordHash;
    String fieldSalt;
    String email;
    Long challengeId;


}
