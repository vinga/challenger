package com.kameo.challenger.odb;


import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;

import javax.persistence.*;

@Entity
public @Data class ChallengeContractODB implements IIdentity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    @ManyToOne
    UserODB first;
    @ManyToOne
    UserODB second;
    String label;


    @Enumerated
    ChallengeContractStatus challengeContractStatus;

}
