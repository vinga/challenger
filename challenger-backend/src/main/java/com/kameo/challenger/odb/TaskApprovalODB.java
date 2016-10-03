package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.crsh.cli.Man;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@ToString(of = IIdentity.id_column)
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

}