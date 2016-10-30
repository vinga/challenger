package com.kameo.challenger.domain.tasks

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.tasks.db.*
import lombok.Data
import java.util.*


interface ITaskRestService {

    @Data
    data class TaskProgressDTO(val taskId: Long = 0, val progressTime: Long = 0, val done: Boolean = false) {

        companion object {
            fun fromOdb(odb: TaskProgressODB): TaskProgressDTO {
                val tp = TaskProgressDTO(taskId = odb.task.id,
                        progressTime = odb.progressTime.time,
                        done = odb.done)
                return tp
            }
        }

    }

    @Data
    data class TaskApprovalDTO(val taskId: Long = 0, val userId: Long = 0, val taskStatus: TaskStatus? = null, val rejectionReason: String? = null) {


        companion object {
            fun fromODBtoDTO(odb: TaskApprovalODB): TaskApprovalDTO {
                val dto = TaskApprovalDTO(
                        taskId = odb.task.id,
                        userId = odb.user.id,
                        rejectionReason = odb.rejectionReason,
                        taskStatus = odb.taskStatus
                )
                return dto
            }
        }
    }

    @Data
    data class TaskDTO(
            val id: Long = 0,
            val label: String = "",
            val icon: String? = null,
            val difficulty: Int = 0, //0-2
            val challengeId: Long = 0,
            val dueDate: Long? = null,
            val userId: Long = 0,
            val createdByUserId: Long = 0,
            val taskType: TaskType = TaskType.daily,
            val taskStatus: TaskStatus = TaskStatus.waiting_for_acceptance,
            val done: Boolean = false,
            val deleted: Boolean? = null,
            var taskApproval: TaskApprovalDTO? = null) {

        companion object {

            fun fromOdb(odb: TaskODB): TaskDTO {
                return TaskDTO(
                        id = odb.id,
                        label = odb.label,
                        icon = odb.icon,
                        difficulty = odb.difficulty,
                        challengeId = odb.challenge.id,
                        dueDate = odb.dueDate?.time ?: null,
                        taskType = odb.taskType,
                        taskStatus = odb.taskStatus,
                        userId = odb.user.id,
                        done = odb.done,
                        createdByUserId = odb.createdByUser.id);
            }
            
            
        }
        fun toODB(): TaskODB {
            var task = TaskODB()
            task.taskStatus = this.taskStatus
            task.taskType = this.taskType
            task.icon = this.icon
            task.label = this.label
            task.challenge = ChallengeODB(this.challengeId)
            task.user = UserODB(this.userId)
            task.createdByUser = UserODB(this.createdByUserId)
            task.difficulty = this.difficulty
            if (this.dueDate != null)
                task.dueDate = Date(this.dueDate)
            if (this.id > 0)
                task.id = this.id
            return task;
        }


    }

}