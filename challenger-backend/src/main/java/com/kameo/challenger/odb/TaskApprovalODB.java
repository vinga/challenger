package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import com.kameo.challenger.web.rest.api.IChallengerService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.crsh.cli.Man;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static com.kameo.challenger.odb.TaskApprovalODB_.rejectionReason;

@Data
@NoArgsConstructor
@ToString(of = "id")
@Entity
public class TaskApprovalODB implements IIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @ManyToOne
    TaskODB task;

    @NotNull
    @ManyToOne
    UserODB user;

    @NotNull
    @Enumerated
    TaskStatus taskStatus;

    String rejectionReason;


    @Override
    public boolean isNew() {
        return getId()<0;
    }


}