package com.kameo.challenger.domain.tasks

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.EventGroupDAO
import com.kameo.challenger.domain.events.EventType.CREATE_TASK
import com.kameo.challenger.domain.tasks.ITaskRestService.*
import com.kameo.challenger.logic.ChallengerLogic
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus.*
import com.kameo.challenger.web.rest.ChallengerSess
import com.kameo.challenger.web.rest.MultiUserChallengerSess
import org.joda.time.format.DateTimeFormat
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path(ServerConfig.restPath)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TaskRestService : ITaskRestService {
    @Inject
    lateinit private var session: ChallengerSess;
    @Inject
    lateinit internal var multiSessions: Provider<MultiUserChallengerSess>;
    @Inject
    lateinit var challengerLogic: ChallengerLogic;
    @Inject
    lateinit var taskDao: TaskDAO;

    @Inject
    lateinit var eventGroupDao: EventGroupDAO;

    @GET
    @Path("challenges/{challengeId}/tasks/")
    fun getTasks(@PathParam("challengeId") contractId: Long, @QueryParam("day")/*"date_yy-MM-dd"*/ dateString: String): List<TaskDTO> {
        val callerId = session.userId
        val date = DateTimeFormat.forPattern("yy-MM-dd").parseDateTime(dateString).toDate()

        val tasks = challengerLogic.getTasks(callerId, contractId, date)
        val taskApprovalODBs = challengerLogic.getTasksApprovalForRejectedTasks(tasks)
        val taskDTOs = tasks.sortedBy { it.id }.map({ TaskDTO.fromOdb(it) })

        taskApprovalODBs.forEach { taskApprovalODB ->
            taskDTOs.filter({ it.id == taskApprovalODB.task.id })
                    .forEach { it.taskApproval = TaskApprovalDTO.fromODBtoDTO(taskApprovalODB) }
        }
        return taskDTOs
    }


    @POST
    @WebResponseStatus(CREATED)
    @Path("/challenges/{challengeId}/tasks/")
    fun createTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long, taskDTO: TaskDTO): TaskDTO? {
        val callerId = session.userId
        var challengeTaskODB = challengerLogic.createTask(callerId, taskDTO.toODB())
        eventGroupDao.createTaskEventAfeterServerAction(challengeTaskODB, CREATE_TASK);
        return TaskDTO.fromOdb(challengeTaskODB)
    }

    @PUT
    @Path("/challenges/{challengeId}/tasks/{taskId}")
    fun updateTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long, taskDTO: TaskDTO): TaskDTO? {
        if (challengeId != taskDTO.challengeId || taskId != taskDTO.id)
            throw IllegalArgumentException();
        val callerId = session.userId
        var challengeTaskODB = challengerLogic.updateTask(callerId, taskDTO.toODB())
        return TaskDTO.fromOdb(challengeTaskODB)
    }

    @DELETE
    @WebResponseStatus(SUCCESSFULLY_DELETED)
    @Path("/challenges/{challengeId}/tasks/{taskId}")
    fun deleteTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long) {
        val callerId = session.userId
        taskDao.deleteTask(callerId, taskId)
    }



    @POST
    @Path("/challenges/{challengeId}/tasks/{taskId}/taskStatus")
    fun changeTaskStatus(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long,ta: TaskApprovalDTO): TaskDTO {
        if (taskId!=ta.taskId)
            throw IllegalArgumentException();
        //TODO check also challenge
        val userIds = multiSessions.get().userIds
        val taskODB = challengerLogic.changeTaskStatus(ta.taskId, userIds, ta.taskStatus, ta.rejectionReason)
        return TaskDTO.fromOdb(taskODB)
    }


    @POST
    @Path("/challenges/{challengeId}/tasks/{taskId}/taskProgress")
    fun updateTaskProgress(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long, tp: TaskProgressDTO): TaskProgressDTO {
        if (taskId!=tp.taskId)
            throw IllegalArgumentException();
        //TODO check also challenge
        val callerId = session.userId
        val tpOdb = challengerLogic.markTaskDone(callerId, tp.taskId, Date(tp.progressTime), tp.done)
        return TaskProgressDTO.fromOdb(tpOdb)
    }


}