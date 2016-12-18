package com.kameo.challenger.domain.tasks

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.events.EventGroupDAO
import com.kameo.challenger.domain.events.EventGroupDAO.TaskCheckUncheckEventInfo
import com.kameo.challenger.domain.events.EventGroupDAO.TaskRejectedEventInfo
import com.kameo.challenger.domain.events.db.EventType.*
import com.kameo.challenger.domain.tasks.ITaskRestService.*
import com.kameo.challenger.domain.tasks.db.TaskStatus.accepted
import com.kameo.challenger.logic.ChallengerLogic
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus.CREATED
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus.SUCCESSFULLY_DELETED
import com.kameo.challenger.web.rest.ChallengerSess
import com.kameo.challenger.web.rest.MultiUserChallengerSess
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.*
import javax.ws.rs.POST
import javax.ws.rs.core.MediaType

@Component
@Path(ServerConfig.restPath)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TaskRestService : ITaskRestService {
    @Inject
    lateinit private var session: ChallengerSess
    @Inject
    lateinit internal var multiSessions: Provider<MultiUserChallengerSess>
    @Inject
    lateinit var challengerLogic: ChallengerLogic
    @Inject
    lateinit var taskDao: TaskDAO

    @Inject
    lateinit var eventGroupDao: EventGroupDAO


    @GET
    @Path("challenges/{challengeId}/taskProgresses")
    fun getTasks(@PathParam("challengeId") contractId: Long, @QueryParam("day") /*"date_yy-MM-dd"*/ dateString: String, @QueryParam("loadTasks") loadTasks: Boolean?): List<TaskProgressDTO> {


        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yy-MM-dd"))

        return getTasksForDay(contractId,dateString).map { TaskProgressDTO.fromTaskOdb(it, it.done, date,loadTasks?: false) }
    }

    @GET
    @Path("challenges/{challengeId}/tasks")
    fun getTasks(@PathParam("challengeId") contractId: Long, @QueryParam("ids") taskIds: String): List<TaskDTO> {
        val callerId = session.userId
        val taskIdsAsLong = taskIds.split(",").map{it.toLong()};
        val tasks =  taskDao.getTasksById(callerId,contractId,taskIdsAsLong);

        var taskApprovalsOtherThanAccepted=taskDao.getTasksApprovalsOtherThanAccepted(tasks)
                .map {TaskApprovalDTO.fromODBtoDTO(it) }.groupBy { it.taskId }.mapValues { it.value.toTypedArray()  }

        return tasks.map {
            TaskDTO.fromOdb(it).apply {
                taskApprovals=taskApprovalsOtherThanAccepted[it.id]
            }
        }

    }


    //deprecated
    @GET
    @Path("challenges/{challengeId}/tasksForDay/")
    fun getTasksForDay(@PathParam("challengeId") contractId: Long, @QueryParam("day") /*"date_yy-MM-dd"*/ dateString: String): List<TaskDTO> {
        val callerId = session.userId
        // val date = DateTimeFormat.forPattern("yy-MM-dd").parseDateTime(dateString).toDate()
        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yy-MM-dd"))
        val tasks = taskDao.getTasks(callerId, contractId, date)


        var taskApprovalsOtherThanAccepted=taskDao.getTasksApprovalsOtherThanAccepted(tasks).groupBy { it.task.id }

        //TODO delete this
        val taskApprovalODBs = challengerLogic.getTasksApprovalForRejectedTasks(tasks)
        val taskDTOs = tasks.sortedBy { it.id }.map {

            var taskDto=TaskDTO.fromOdb(it)

            taskDto.taskApprovals=taskApprovalsOtherThanAccepted[it.id]?.map {
                TaskApprovalDTO.fromODBtoDTO(it)
            }?.toTypedArray()


            taskDto
        }




        //TODO delete this
        taskApprovalODBs.forEach { taskApprovalODB ->
            taskDTOs.filter({ it.id == taskApprovalODB.task.id })
                    .forEach { it.taskApproval = TaskApprovalDTO.fromODBtoDTO(taskApprovalODB) }
        }
        return taskDTOs
    }


    @POST
    @WebResponseStatus(CREATED)
    @Path("/challenges/{challengeId}/tasks/")
    fun createTask(@PathParam("challengeId") challengeId: Long, taskDTO: TaskDTO): TaskDTO? {
        val callerId = session.userId
        if (challengeId != taskDTO.challengeId)
            throw IllegalArgumentException()
        var challengeTaskODB = taskDao.createTask(callerId, taskDTO.toODB())
        eventGroupDao.createTaskEventAfterServerAction(task = challengeTaskODB, eventType = CREATE_TASK)
        return TaskDTO.fromOdb(challengeTaskODB)
    }

    @PUT
    @Path("/challenges/{challengeId}/tasks/{taskId}")
    fun updateTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long, taskDTO: TaskDTO): TaskDTO? {
        if (challengeId != taskDTO.challengeId || taskId != taskDTO.id)
            throw IllegalArgumentException()
        val callerId = session.userId
        var challengeTaskODB = challengerLogic.updateTask(callerId, taskDTO.toODB())
        eventGroupDao.createTaskEventAfterServerAction(user_ = challengerLogic.getUserById(callerId), task = challengeTaskODB, eventType = UPDATE_TASK)
        return TaskDTO.fromOdb(challengeTaskODB)
    }

    @DELETE
    @WebResponseStatus(SUCCESSFULLY_DELETED)
    @Path("/challenges/{challengeId}/tasks/{taskId}")
    fun deleteTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long) {
        val callerId = session.userId
        var task = taskDao.deleteTask(callerId, taskId)
        eventGroupDao.createTaskEventAfterServerAction(user_ = challengerLogic.getUserById(callerId), task = task, eventType = DELETE_TASK)
    }


    @POST
    @Path("/challenges/{challengeId}/tasks/{taskId}/taskStatus")
    fun changeTaskStatus(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long, ta: TaskApprovalDTO): TaskDTO {
        if (taskId != ta.taskId)
            throw IllegalArgumentException()

        val userIds = multiSessions.get().userIds
        val taskODB = taskDao.changeTaskStatus(challengeId, ta.taskId, userIds, ta.taskStatus, ta.rejectionReason)

        for (userId in userIds) {
            if (ta.taskStatus == accepted)
                eventGroupDao.createTaskEventAfterServerAction(user_ = challengerLogic.getUserById(userId), task = taskODB, eventType = ACCEPT_TASK)
            else
                eventGroupDao.createTaskEventAfterServerAction(user_ = challengerLogic.getUserById(userId), task = taskODB, eventType = REJECT_TASK, eventInfo =
                TaskRejectedEventInfo(ta.rejectionReason!!))
        }
        return TaskDTO.fromOdb(taskODB)
    }


    @POST
    @Path("/challenges/{challengeId}/tasks/{taskId}/taskProgress")
    fun updateTaskProgress(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long, tp: TaskProgressDTO): TaskProgressDTO {
        if (taskId != tp.taskId)
            throw IllegalArgumentException()
        val callerId = session.userId
        val tpOdb = taskDao.markTaskDone(callerId, challengeId, tp.taskId, tp.toLocalDate(), tp.done)
        val eventType = if (tp.done) CHECKED_TASK else UNCHECKED_TASK

        eventGroupDao.createTaskEventAfterServerAction(
                user_ = challengerLogic.getUserById(callerId),
                task = tpOdb.task,
                eventType = eventType,
                eventInfo = TaskCheckUncheckEventInfo(tp.toLocalDate()))

        return TaskProgressDTO.fromOdb(tpOdb)
    }


    @PUT
    @Path("/challenges/{challengeId}/tasks/{taskId}/close")
    fun closeTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long) {
        val callerId = session.userId
        val taskODB = taskDao.closeTask(callerId, taskId);
        eventGroupDao.createTaskEventAfterServerAction(
                user_ = challengerLogic.getUserById(callerId),
                task = taskODB,
                eventType = CLOSE_TASK)
    }
}