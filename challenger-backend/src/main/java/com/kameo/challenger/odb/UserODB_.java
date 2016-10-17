package com.kameo.challenger.odb;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(UserODB.class)
public class UserODB_ {
    public static volatile SingularAttribute<ChallengeODB, UserODB> createdBy;
    public static volatile SingularAttribute<ChallengeODB, Long> id;
    public static volatile SingularAttribute<ChallengeODB, String> label;
    public static volatile SingularAttribute<ChallengeODB, ChallengeStatus> challengeStatus;
    public static volatile ListAttribute<ChallengeODB, ChallengeParticipantODB> participants;
}



