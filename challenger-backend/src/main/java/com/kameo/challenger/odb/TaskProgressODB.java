package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(indexes = {
    @Index(unique = true, columnList = "task_id, progressTime")
})
public class TaskProgressODB implements IIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    Date progressTime;

    boolean done;


    @NotNull
    @ManyToOne
    TaskODB task;

}
