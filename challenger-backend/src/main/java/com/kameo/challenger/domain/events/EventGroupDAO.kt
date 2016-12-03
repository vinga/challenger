package com.kameo.challenger.domain.events

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.events.db.EventODB
import com.kameo.challenger.domain.events.db.EventReadODB
import com.kameo.challenger.domain.events.db.EventType
import com.kameo.challenger.domain.events.db.EventType.CHECKED_TASK
import com.kameo.challenger.domain.events.db.EventType.POST
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

@Component
@Transactional
open class EventGroupDAO(@Inject val anyDaoNew: AnyDAONew,
                         @Inject val serverConfig: ServerConfig,
                         @Inject val eventPushDao: Provider<EventPushDAO>,
                         @Inject val permissionDao: PermissionDAO) {

    interface IEventInfo

    class TaskCheckUncheckEventInfo(val taskCheckUncheckDate: LocalDate, val checkDate: LocalDate = LocalDate.now()) : IEventInfo
    class TaskRejectedEventInfo(val rejectionReason: String) : IEventInfo

    open fun createTaskEventAfterServerAction(user_: UserODB? = null, task: TaskODB, eventType: EventType, eventInfo: IEventInfo? = null) {
        val user = user_ ?: anyDaoNew.em.find(UserODB::class.java, task.createdByUser.id)
        val e = EventODB()
        e.eventType = eventType
        e.content = when (eventType) {
            EventType.CREATE_TASK -> user.getLoginOrEmail() + " created " + task.label
            EventType.POST -> e.content
            EventType.UPDATE_TASK -> user.getLoginOrEmail() + " updated " + task.label
            EventType.ACCEPT_TASK -> user.getLoginOrEmail() + " accepted " + task.label
            EventType.REJECT_TASK ->
                user.getLoginOrEmail() + " rejected " + task.label + " because of " + (eventInfo as TaskRejectedEventInfo).rejectionReason
            EventType.UNCHECKED_TASK,
            EventType.CHECKED_TASK -> {
                val effectiveDay = (eventInfo!! as TaskCheckUncheckEventInfo).taskCheckUncheckDate
                val checkDay = (eventInfo!! as TaskCheckUncheckEventInfo).checkDate
                e.forDay=effectiveDay
                val daystring = if (!effectiveDay.isEqual(checkDay))
                    " for " + DateTimeFormatter.ofPattern("dd MMM").withLocale(Locale.ENGLISH).format(effectiveDay)
                else ""
                val actionType=if (eventType==CHECKED_TASK) "checked" else "unchecked"


                "${user.getLoginOrEmail()} $actionType ${task.label}$daystring"
            }

            EventType.DELETE_TASK -> user.getLoginOrEmail() + " deleted " + task.label
        }
        e.challenge = task.challenge
        e.author = user
        e.taskId = task?.id

        anyDaoNew.em.persist(e)

        anyDaoNew.find(ChallengeODB::class, task.challenge.id).participants.forEach {
            anyDaoNew.persist(EventReadODB(it.user, it.challenge, e))
        }

        eventPushDao.get().broadcastNewEvent(task.challenge.id)
    }

    open fun createEventFromClient(callerId: Long, challengeId: Long, p: EventODB): EventODB {
        if (!p.isNew())
            throw IllegalArgumentException()
        if (challengeId != p.challenge.id)
            throw IllegalArgumentException()
        if (p.eventType != POST)
            throw IllegalArgumentException()
        permissionDao.checkHasPermissionToChallenge(callerId, p.challenge.id)
        checkHasPermissionToTaskIfExists(callerId, p)



        anyDaoNew.persist(p)

        anyDaoNew.find(ChallengeODB::class, challengeId).participants.forEach {
            val er = EventReadODB(it.user, it.challenge, p)
            anyDaoNew.persist(er)
        }

        eventPushDao.get().broadcastNewEvent(challengeId)
        return p
    }


    open fun markEventAsRead(callerId: Long, challengeId: Long, eventId: Long, readDate: Date) {
        anyDaoNew.update(EventReadODB::class) {
            it.set(EventReadODB::read, readDate)
            it.get(EventReadODB::event) eqId eventId
            it get (EventReadODB::user) eqId callerId
            it get (EventReadODB::challenge) eqId challengeId
            it.get(EventReadODB::read).isNull()
        }
    }


    open fun getEventsForTask(callerId: Long, challengeId: Long, taskId: Long): List<EventODB> {
        permissionDao.checkHasPermissionToTask(callerId, taskId)
        return anyDaoNew.getAll(EventODB::class) {
            it get EventODB::taskId eq taskId
            it get EventODB::challenge eqId challengeId
        }
    }


    open fun getUnreadEventsForChallenge(callerId: Long, challengeId: Long, maxEvents: Int? = null): List<Pair<EventReadODB, EventODB>> {


        return anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
            it.get(EventReadODB::read).isNull()
            it orderByAsc +EventReadODB::id
            it.select(it, it get EventReadODB::event)
        }.sortedWith(sortEventsReadAsc())
    }


    open fun getLaterEventsForChallenge(callerId: Long, challengeId: Long, lastReadEventId: Long, maxEvents: Int? = null): List<Pair<EventReadODB, EventODB>> {
        return anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
            it get +EventReadODB::event get +EventODB::id gt lastReadEventId
            it orderByAsc +EventReadODB::id
            it.select(it, it get EventReadODB::event)
        }.sortedWith(sortEventsReadAsc())
    }


    open fun getLastEventsForChallenge(callerId: Long, challengeId: Long, maxEvents: Int? = null): List<Pair<EventReadODB, EventODB>> {
        permissionDao.checkHasPermissionToChallenge(callerId, challengeId)
        val desiredMaxEvents = maxEvents ?: serverConfig.maxEventsSize
        // first try to fetch all unread
        val firstNotRead = anyDaoNew.getFirst(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
            it.get(EventReadODB::read).isNull()
            it.orderByAsc(+EventReadODB::id)
            it limit 1
        }


        val res = if (firstNotRead == null) {
            // all are read, just return maxEvents of them
            anyDaoNew.getAll(EventReadODB::class) {
                it get EventReadODB::user eqId callerId
                it get EventReadODB::challenge eqId challengeId
                it orderByDesc EventReadODB::read
                it limit desiredMaxEvents
                it.select(it, it get EventReadODB::event )
            }
        } else {
            // at least one is unread, getch it and all later
            anyDaoNew.getAll(EventReadODB::class) {
                it get EventReadODB::user eqId callerId
                it get EventReadODB::challenge eqId challengeId
                it get +EventReadODB::id ge firstNotRead.id
                it.select(it, it.get(EventReadODB::event))
            }.let {
                // in case if it is still to small add to it some previous read message
                val notReadSize = it.size
                if (it.size < desiredMaxEvents) {
                    it + anyDaoNew.getAll(EventReadODB::class) {
                        it get EventReadODB::user eqId callerId
                        it get EventReadODB::challenge eqId challengeId
                        it get +EventReadODB::id lt firstNotRead.id
                        it limit desiredMaxEvents - notReadSize
                        it.select(it, it get EventReadODB::event)
                    }
                } else
                    it
            }
        }
        return res.sortedWith(sortEventsReadAsc())
    }


    private fun sortEventsReadAsc(): Comparator<Pair<EventReadODB, EventODB>> {
        return Comparator<Pair<EventReadODB, EventODB>> { o1, o2 ->
            if (o1.first.read != null && o2.first.read != null)
                o1.first.read!!.compareTo(o2.first.read);
            else if (o1.first.read == null && o2.first.read == null)
                o1.first.id.compareTo(o2.first.id)
            else if (o1.first.read == null)
                -1
            else if (o2.first.read == null)
                1
            else 0
        }
    }


    @Deprecated("old version without post read")
    open fun getPostsForChallengeOld(callerId: Long, challengeId: Long, maxEvents: Int? = null): List<EventODB> {
        permissionDao.checkHasPermissionToChallenge(callerId, challengeId)
        return getPostsForChallengeWithoutPermissionCheck(challengeId, maxEvents)
    }

    open fun getPostsForChallengeWithoutPermissionCheck(challengeId: Long, maxEvents: Int? = null): List<EventODB> {
        return anyDaoNew.getAll(EventODB::class) {
            it get EventODB::challenge eqId challengeId
            it orderByDesc EventODB::createDate
            it limit (maxEvents ?: serverConfig.maxEventsSize)
        }
    }


    private fun checkHasPermissionToTaskIfExists(callerId: Long, p: EventODB) {
        val taskId = p.taskId
        if (taskId != null) {
            permissionDao.checkHasPermissionToTask(callerId, taskId)
            // it ensures that  task & challenge id are correct
            anyDaoNew.exists(TaskODB::class) {
                it eqId taskId
                it get TaskODB::challenge eqId p.challenge.id
            }
        }
    }


}




