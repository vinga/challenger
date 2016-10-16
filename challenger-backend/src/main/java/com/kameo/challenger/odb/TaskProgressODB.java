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

    public TaskODB getTask() {
        return task;
    }

    public void setTask(TaskODB task) {
        this.task = task;
    }

    public Date getProgressTime() {
        return progressTime;
    }

    public void setProgressTime(Date progressTime) {
        this.progressTime = progressTime;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }


    @Override
    public boolean isNew() {
        return getId()<0;
    }
}
