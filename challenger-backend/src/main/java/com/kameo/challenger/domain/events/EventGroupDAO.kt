package com.kameo.challenger.domain.accounts

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.events.EventODB
import com.kameo.challenger.domain.events.EventType
import com.kameo.challenger.domain.events.EventType.CREATE_TASK
import com.kameo.challenger.domain.events.EventType.POST
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.PathWrap
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.inject.Inject
import kotlin.reflect.*
import kotlin.reflect.KMutableProperty1.Setter
import kotlin.reflect.KProperty1.Getter

@Component
@Transactional
open class EventGroupDAO(@Inject val anyDaoNew: AnyDAONew, @Inject val serverConfig: ServerConfig) {


    open fun createTaskEventAfeterServerAction(task: TaskODB, eventType: EventType) {

        var user = anyDaoNew.em.find(UserODB::class.java, task.createdByUser.id);
        val e = EventODB();
        e.eventType = CREATE_TASK;
        e.content = user.getLoginOrEmail() + " created new task " + task.label;
        e.challenge = task.challenge;
        e.author = user;

        anyDaoNew.em.persist(e);
    }

    open fun getEventsForTask(callerId: Long, challengeId: Long, taskId: Long): List<EventODB> {
        checkHasPermissionToTask(callerId, taskId);

        return anyDaoNew.getAll(EventODB::class, {
            it get EventODB::task eqId taskId
            it get EventODB::challenge eqId challengeId

        })
    }

    open fun getPostsForChallenge(callerId: Long, challengeId: Long, maxEvents: Int? = null): List<EventODB> {
        checkHasPermissionToChallenge(callerId, challengeId);

        return anyDaoNew.getAll(EventODB::class, {
            it get EventODB::challenge eqId challengeId

            it orderByDesc EventODB::createDate

            it limit (maxEvents ?: serverConfig.maxEventsSize)


        })
    }
    open fun getPostsForChallengeWithoutPermissionCheck(challengeId: Long, maxEvents: Int? = null): List<EventODB> {

        return anyDaoNew.getAll(EventODB::class, {
            it get EventODB::challenge eqId challengeId

            it orderByDesc EventODB::createDate

            it limit (maxEvents ?: serverConfig.maxEventsSize)


        })
    }
    open fun createEvent(callerId: Long, challengeId: Long, p: EventODB): EventODB {
        if (challengeId != p.challenge.id)
            throw IllegalArgumentException();
        checkHasPermissionToChallenge(callerId, p.challenge.id);

        checkHasPermissionToTaskIfExists(callerId, p)

        anyDaoNew.em.persist(p);

        return p;
    }


    open fun editEvent(callerId: Long, challengeId: Long, p: EventODB): EventODB {
        if (challengeId != p.challenge.id)
            throw IllegalArgumentException();
        if (p.eventType != POST)
            throw IllegalArgumentException();
        checkHasPermissionToChallenge(callerId, p.challenge.id);


        checkHasPermissionToTaskIfExists(callerId, p)


        anyDaoNew.em.merge(p);

        return p;
    }

    private fun checkHasPermissionToTaskIfExists(callerId: Long, p: EventODB) {
        val task = p.task ?: null;
        if (task != null) {
            checkHasPermissionToTask(callerId, task.id);
            // it ensures that  task & challenge id are correct
            anyDaoNew.getOne(TaskODB::class, {
                it eqId task.id
                it get TaskODB::challenge eqId p.challenge.id
            })
        }
    }

    private fun checkHasPermissionToTask(callerId: Long, taskId: Long) {
        val exists = anyDaoNew.exists(TaskODB::class, {
            it eqId taskId
            it.join(TaskODB::challenge).joinList(ChallengeODB::participants)
                    .eqId(ChallengeParticipantODB::user, callerId)
                    .get(ChallengeParticipantODB::challengeStatus) eq ChallengeStatus.ACTIVE
        })
        if (!exists)
            throw IllegalArgumentException("No permission");
    }

    private fun checkHasPermissionToChallenge(callerId: Long, challengeId: Long) {
        val exists = anyDaoNew.exists(ChallengeParticipantODB::class, {
            it get ChallengeParticipantODB::user eqId callerId
            it get ChallengeParticipantODB::challenge eqId challengeId
            it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
        })
        if (!exists)
            throw IllegalArgumentException("No permission");
    }

    fun  getEventsBetween(minEventIdExclusive: Long?, maxEventIdExclusive: Long?): List<EventODB> {

        return anyDaoNew.getAll(EventODB::class, {

            if (minEventIdExclusive!=null) {
                it.after(+EventODB::id, minEventIdExclusive)
            }
            if (maxEventIdExclusive!=null) {
                it.before(+EventODB::id, maxEventIdExclusive)
            }
            it

        })
    }

    fun  getMaxEventId(): Long? {
        return anyDaoNew.getFirst(EventODB::class, {
            it.select(it.max(+EventODB::id))
        })
    }



}




