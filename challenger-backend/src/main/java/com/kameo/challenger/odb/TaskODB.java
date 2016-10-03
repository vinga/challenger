package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@ToString(of = IIdentity.id_column)
@Entity
@Table(indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "challenge_id"),
})
public class TaskODB implements IIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Enumerated
    @NotNull
    private TaskType taskType;
    private String icon;
    private int difficulty;
    private String label;
    @Enumerated
    @NotNull
    private TaskStatus taskStatus;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;
    @ManyToOne
    @NotNull
    private UserODB user;
    @ManyToOne
    @NotNull
    private UserODB createdByUser;

    @Transient
    boolean done;

    @NotNull
    @ManyToOne
    private ChallengeODB challenge;

    public TaskODB(long id) {
        this.id = id;
    }
}
