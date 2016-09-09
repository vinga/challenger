package com.kameo.challenger.odb;


import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(indexes = {
        @Index(columnList = "first_id"),
        @Index(columnList = "second_id"),
})
public @Data class ChallengeODB implements IIdentity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    @ManyToOne
    UserODB first;
    @ManyToOne
    UserODB second;
    String label;

    @Temporal(TemporalType.TIMESTAMP)
    Date lastSeenByFirst;
    @Temporal(TemporalType.TIMESTAMP)
    Date lastSeenBySecond;


    @NotNull
    @Enumerated
    ChallengeStatus challengeStatus;


    public ChallengeODB(long id) {
        this.id = id;
    }

}
