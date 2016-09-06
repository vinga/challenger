package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ToString(of=IIdentity.id_column)
@Entity
public class ChallengeActionODB implements IIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Enumerated
    @NotNull
    private ActionType actionType;
    private String icon;
    private int difficulty;
    private String label;
    @Enumerated
    @NotNull
    private ActionStatus actionStatus;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;
    @ManyToOne
    @NotNull
    private UserODB user;
    @ManyToOne
    private UserODB createdByUser;



    @ManyToOne
    private ChallengeContractODB challengeContract;


}
