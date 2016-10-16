package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import com.kameo.challenger.utils.odb.EntityHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(indexes = {
    @Index(columnList = "user_id"),
    @Index(columnList = "challenge_id"),
})
@ToString(of = "id")
public
class ChallengeParticipantODB implements IIdentity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;


    @NotNull
    @ManyToOne
    ChallengeODB challenge;

    @NotNull
    @ManyToOne
    UserODB user;

    @Temporal(TemporalType.TIMESTAMP)
    Date lastSeen;


    @NotNull
    @Enumerated
    ChallengeStatus challengeStatus;

    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChallengeParticipantODB && ((ChallengeParticipantODB) obj).getId()==this.getId();
    }

    public ChallengeODB getChallenge() {
        return challenge;
    }

    public void setChallenge(ChallengeODB challenge) {
        this.challenge = challenge;
    }

    public UserODB getUser() {
        return user;
    }

    public void setUser(UserODB user) {
        this.user = user;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }


    @Override
    public boolean isNew() {
        return getId()<0;
    }
}
