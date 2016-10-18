package com.kameo.challenger.odb;


import com.google.common.collect.Lists;
import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@ToString(of = "id")
public @Data class ChallengeODB implements IIdentity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;

    String label;

    @NotNull
    @ManyToOne
    UserODB createdBy;


    @Override
    public long getId() {
        return id;
    }

    @NotNull
    @Enumerated
    ChallengeStatus challengeStatus;


    @OneToMany(mappedBy = "challenge")
    List<ChallengeParticipantODB> participants= Lists.newArrayList();


    public ChallengeODB(long id) {
        this.id = id;
    }

    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChallengeODB && ((ChallengeODB) obj).getId()==this.getId();
    }

    @Override
    public boolean isNew() {
        return getId()<0;
    }
}
