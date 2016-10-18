package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;


@NoArgsConstructor
@ToString(of = "id")
@Entity
@Table(indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "challenge_id"),
})
@Data
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

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return getId()<0;
    }
}
