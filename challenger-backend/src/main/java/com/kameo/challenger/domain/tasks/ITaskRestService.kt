package com.kameo.challenger.domain.tasks

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.tasks.db.*
import com.kameo.challenger.domain.tasks.db.TaskStatus.accepted
import lombok.Data
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset


interface ITaskRestService {

    @Data
    data class TaskProgressDTO(val taskId: Long = 0, val progressTime: Long = 0, val done: Boolean = false, val task: TaskDTO? = null) {

        companion object {
            fun fromOdb(odb: TaskProgressODB, withTask: Boolean = false): TaskProgressDTO {


                val tp = TaskProgressDTO(taskId = odb.task.id,
                        progressTime = odb.progressTime.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                        done = odb.done,
                        task=if (withTask) TaskDTO.fromOdb(odb.task) else null)
                return tp
            }
            fun fromTaskOdb(task: TaskDTO, done: Boolean, localDate: LocalDate, withTask: Boolean = false): TaskProgressDTO {


                val tp = TaskProgressDTO(taskId = task.id,
                        progressTime  = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                        done = done,
                        task=if (withTask)task else null)
                return tp
            }

        }
        fun toLocalDate(): LocalDate {
            return Instant.ofEpochMilli(progressTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    @Data
    data class TaskApprovalDTO(val taskId: Long = 0, val userId: Long = 0, val taskStatus: TaskStatus = accepted, val rejectionReason: String? = null) {


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
            val closeDate: Long? = null,
            val deleted: Boolean? = null,
            var taskApproval: TaskApprovalDTO? = null,
            var taskApprovals: Array<TaskApprovalDTO>? = null,
            var monthDays: String? = null,
            var weekDays: String? = null
     )

    {

        companion object {

            fun fromOdb(odb: TaskODB): TaskDTO {
                return TaskDTO(
                        id = odb.id,
                        label = odb.label,
                        icon = odb.icon,
                        difficulty = odb.difficulty,
                        challengeId = odb.challenge.id,
                        dueDate = odb.dueDate?.toInstant(ZoneOffset.UTC)?.toEpochMilli(),
                        taskType = odb.taskType,
                        taskStatus = odb.taskStatus,
                        userId = odb.user.id,
                        done = odb.done,
                        closeDate =  odb.closeDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli(),
                        createdByUserId = odb.createdByUser.id,
                        monthDays = odb.monthDays,
                        weekDays = odb.weekDays

                        )

            }
            
            
        }
        fun toODB(): TaskODB {
            val task = TaskODB()
            task.taskStatus = this.taskStatus
            task.taskType = this.taskType
            task.icon = this.icon
            task.label = this.label
            task.challenge = ChallengeODB(this.challengeId)
            task.user = UserODB(this.userId)
            task.createdByUser = UserODB(this.createdByUserId)
            task.difficulty = this.difficulty
            if (this.dueDate != null)
                task.dueDate =  Instant.ofEpochMilli(this.dueDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
            if (this.id > 0)
                task.id = this.id

            task.monthDays = this.monthDays
            task.weekDays = this.weekDays
            return task
        }


    }

}