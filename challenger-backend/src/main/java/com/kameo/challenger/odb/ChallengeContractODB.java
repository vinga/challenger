package com.kameo.challenger.odb;


import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@NoArgsConstructor
public @Data class ChallengeContractODB implements IIdentity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    @ManyToOne
    UserODB first;
    @ManyToOne
    UserODB second;
    String label;


    @NotNull
    @Enumerated
    ChallengeContractStatus challengeContractStatus;


    public ChallengeContractODB(long id) {
        this.id = id;
    }

}
