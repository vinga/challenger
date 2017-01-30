package com.kameo.challenger.domain.events

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus.REFUSED
import com.kameo.challenger.domain.challenges.db.ChallengeStatus.REMOVED
import com.kameo.challenger.domain.events.db.EventODB
import com.kameo.challenger.domain.events.db.EventReadODB
import com.kameo.challenger.domain.events.db.EventType
import com.kameo.challenger.domain.events.db.EventType.*
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import rx.AsyncEmitter
import rx.AsyncEmitter.Cancellable
import rx.Observable
import rx.Subscription
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Provider


@Component
@Transactional
open class EventGroupDAO(@Inject val anyDaoNew: AnyDAONew,
                         @Inject val serverConfig: ServerConfig,
                         @Inject val eventPushDao: Provider<EventPushDAO>,
                         @Inject val permissionDao: PermissionDAO,
                         @Inject val appEventPublisher: ApplicationEventPublisher) {

    interface IEventInfo

    class TaskCheckUncheckEventInfo(val taskCheckUncheckDate: LocalDate, val checkDate: LocalDate = LocalDate.now()) : IEventInfo
    class TaskRejectedEventInfo(val rejectionReason: String) : IEventInfo
    class ChallengeInviteRemoveUserEventInfo(val user: UserODB) : IEventInfo


    open fun createChallengeEventAfterServerAction(userId: Long, challenge: ChallengeODB, eventType: EventType, eventInfo: ChallengeInviteRemoveUserEventInfo? = null) {
        val user = anyDaoNew.find(UserODB::class, userId)
        val e = EventODB()
        e.eventType = eventType
        e.content = when (eventType) {
            EventType.ACCEPT_CHALLENGE -> user.getLoginOrEmail() + " accepted challenge " + challenge.label
            EventType.REJECT_CHALLENGE -> user.getLoginOrEmail() + " rejected challenge " + challenge.label
            EventType.REMOVE_CHALLENGE -> user.getLoginOrEmail() + " deleted challenge " + challenge.label
            EventType.UPDATE_CHALLENGE -> user.getLoginOrEmail() + " updated challenge " + challenge.label
            EventType.INVITE_USER_TO_CHALLENGE -> {
                e.affectedUser = (eventInfo as ChallengeInviteRemoveUserEventInfo).user
                user.getLoginOrEmail() + " invited user " + e.affectedUser!!.getLoginOrEmail() + " to challenge " + challenge.label
            }
            EventType.REMOVE_USER_FROM_CHALLENGE -> {
                e.affectedUser = (eventInfo as ChallengeInviteRemoveUserEventInfo).user
                user.getLoginOrEmail() + " removed user " + e.affectedUser!!.getLoginOrEmail() + " from challenge " + challenge.label
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
        e.challenge = challenge
        e.author = user
        anyDaoNew.em.persist(e)



        publish(MetaEventDTO(e, challengeParticipantsThatReveiveEvents(challenge).map { it.user }))


    }

    open fun publish(metaEvent: EventGroupDAO.MetaEventDTO) {
        /*     metaEvent.users.forEach {
                  it.saveParticipantEventRead(metaEvent.event, metaEvent.event.challenge)
              }

              eventPushDao.get().broadcastNewEvent(metaEvent.event.challenge.id)*/


        appEventPublisher.publishEvent(metaEvent)
    }

    class MetaEventDTO(val event: EventODB, val users: List<UserODB>);

    @Inject
    lateinit var txManager: PlatformTransactionManager;

    @Suppress("unused") // used, it's listener function
    @TransactionalEventListener(phase = AFTER_COMMIT)
    open fun handleOrderCreatedEvent(metaEvent: MetaEventDTO) {


        //should ensure that for current user (user.id) all eventReads will have increased id in proper order
        metaEvent.users.forEach {
            synchronized(it.id) {
                //thread safe by user.id (not sure this id-Long is unique within system (single JVM), it should be)
                //this is in order to keep always eventRead.id increasing for any user
                println("handleOrderCreatedEvent-start "+it.id)
                val txTemplate = TransactionTemplate(txManager)
                txTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
                txTemplate.execute(object : TransactionCallback<Any> {
                    override fun doInTransaction(status: TransactionStatus) {

                        anyDaoNew.persist(EventReadODB(firstReadId.incrementAndGet(), it, metaEvent.event.challenge, metaEvent.event))
                        // Thread.sleep(10000)


                    }
                })
                println("handleOrderCreatedEvent-end "+it.id)
            }
        }


        eventPushDao.get().broadcastNewEvent(metaEvent.event.challenge.id)
    }



    private val firstReadId: AtomicLong by lazy {
        var maxReadId: Long = anyDaoNew.getFirst(EventReadODB::class) {
            it.select(it.max(+EventReadODB::id))
        } ?: 0;
        AtomicLong(maxReadId + 1)
    }

    open fun getMaxEventReadId(callerId: Long): Long {
        var maxId: Long = anyDaoNew.getFirst(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it.select(it.max(+EventReadODB::id))
        } ?: -1;
        return maxId;
    }


    private fun challengeParticipantsThatReveiveEvents(challenge: ChallengeODB) =
            anyDaoNew.find(ChallengeODB::class, challenge.id).participants.filter { it.challengeStatus != REFUSED && it.challengeStatus != REMOVED }

    open fun createGlobalEventAfterServerAction(callerId: Long, challengeODB: ChallengeODB, eventType: EventType, eventInfo: IEventInfo? = null, vararg recipients: UserODB) {
        val user = anyDaoNew.find(UserODB::class, callerId)
        val e = EventODB()
        e.eventType = eventType
        e.challenge = challengeODB
        e.author = user
        e.content = when (eventType) {
            REMOVE_ME_FROM_CHALLENGE -> {
                e.affectedUser = (eventInfo as ChallengeInviteRemoveUserEventInfo).user
                user.getLoginOrEmail() + " removed you from challenge " + e.challenge.label
            }
            else -> throw IllegalArgumentException()
        }
        anyDaoNew.persist(e)


        publish(MetaEventDTO(e, challengeParticipantsThatReveiveEvents(challengeODB).map { it.user }))

        //recipients.forEach { it.saveParticipantEventRead(e, challengeODB) }

        // eventPushDao.get().broadcastNewEvent(challengeODB.id)
    }

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
                val effectiveDay = (eventInfo as TaskCheckUncheckEventInfo).taskCheckUncheckDate
                val checkDay = eventInfo.checkDate
                e.forDay = effectiveDay
                val daystring = if (!effectiveDay.isEqual(checkDay))
                    " for " + DateTimeFormatter.ofPattern("dd MMM").withLocale(Locale.ENGLISH).format(effectiveDay)
                else ""
                val actionType = if (eventType == CHECKED_TASK) "checked" else "unchecked"


                "${user.getLoginOrEmail()} $actionType ${task.label}$daystring"
            }

            EventType.DELETE_TASK -> user.getLoginOrEmail() + " deleted " + task.label
            EventType.CLOSE_TASK -> user.getLoginOrEmail() + " closed " + task.label
            else -> throw IllegalArgumentException()

        }
        e.challenge = task.challenge
        e.author = user
        e.taskId = task.id

        anyDaoNew.persist(e)


        publish(MetaEventDTO(e, challengeParticipantsThatReveiveEvents(e.challenge).map { it.user }))
        //challengeParticipantsThatReveiveEvents(e.challenge).saveParticipants(e);

        //eventPushDao.get().broadcastNewEvent(task.challenge.id)
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

        publish(MetaEventDTO(p, challengeParticipantsThatReveiveEvents(anyDaoNew.find(ChallengeODB::class, challengeId)).map { it.user }))
        // challengeParticipantsThatReveiveEvents(anyDaoNew.find(ChallengeODB::class, challengeId)).saveParticipants(p);


        // eventPushDao.get().broadcastNewEvent(challengeId)
        return p
    }


    open fun markEventAsRead(callerId: Long, challengeId: Long, eventId: Long, readDate: Date) {
        anyDaoNew.update(EventReadODB::class) {
            it.set(EventReadODB::read, readDate)
            it get EventReadODB::event eqId eventId
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
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


    open fun getUnreadEventsForChallenge(callerId: Long, challengeId: Long?): List<Pair<EventReadODB, EventODB>> {
        return anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            if (challengeId != null)
                it get EventReadODB::challenge eqId challengeId
            it.get(EventReadODB::read).isNull()
            it orderByAsc +EventReadODB::id
            it.select(it, it get EventReadODB::event)
        }.sortedWith(sortEventsReadAsc())
    }


    open fun getLaterEventsForChallenge(callerId: Long, challengeId: Long?, lastReadEventId: Long): List<Pair<EventReadODB, EventODB>> {
        return anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            if (challengeId != null)
                it get EventReadODB::challenge eqId challengeId

            it get +EventReadODB::id gt lastReadEventId
            it orderByAsc +EventReadODB::id
            it.select(it, it get EventReadODB::event)
        }.sortedWith(sortEventsReadAsc())
    }


    /**
     * this is sync call, to fetch history events (posts & actions) when challenge is loaded
     * global events like REMOVE_ME_FROM_CHALLENGE or DELETE CHALLENGE are returned only from async call, cause removed user won't call method for challenge he/she was removed
     */
    open fun getLastEventsForChallenge(callerId: Long, challengeId: Long, beforeEventReadId: Long? = null, maxEvents: Int?): List<Pair<EventReadODB, EventODB>> {
        permissionDao.checkHasPermissionToChallenge(callerId, challengeId)
        val desiredMaxEvents = maxEvents ?: serverConfig.maxEventsSize

        val res = anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
            it get +EventReadODB::id beforeNotNull beforeEventReadId

            it orderByDesc EventReadODB::read
            it orderBy (Pair(it get +EventReadODB::id, false))

            it limit desiredMaxEvents
            it.select(it, it get EventReadODB::event)
        }


        /*
        // first try to fetch all unread

        val firstNotRead =
            if (beforeEventId == null)
                anyDaoNew.getFirst(EventReadODB::class) {
                    it get EventReadODB::user eqId callerId
                    it get EventReadODB::challenge eqId challengeId
                    it.get(EventReadODB::read).isNull()
                    it.orderByAsc(+EventReadODB::id)
                    it limit 1
                }
            else null // ignore if read or urnead, just fetch all before beforeEventId


    val res = if (firstNotRead == null) {
        // all are read, just return maxEvents of them
        anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
            it get +EventReadODB::event get +EventODB::id beforeNotNull beforeEventId

            it orderByDesc EventReadODB::read
            it orderBy(Pair(it get +EventReadODB::event get +EventODB::id,false))

            it limit desiredMaxEvents
            it.select(it, it get EventReadODB::event)
        }
    } else {
        // at least one is unread, getch it and all later
        anyDaoNew.getAll(EventReadODB::class) {
            it get EventReadODB::user eqId callerId
            it get EventReadODB::challenge eqId challengeId
            it get +EventReadODB::id ge firstNotRead.id
            it.select(it, it.get(EventReadODB::event))
        }.let {
            // in case if it is still too small add to it some previous read message
            val notReadSize = it.size
            if (it.size < desiredMaxEvents) {
                it + anyDaoNew.getAll(EventReadODB::class) {
                    val er = this
                    er get EventReadODB::user eqId callerId
                    er get EventReadODB::challenge eqId challengeId
                    er get +EventReadODB::id lt firstNotRead.id
                    er limit desiredMaxEvents - notReadSize
                    er.select(er, er get EventReadODB::event)
                }
            } else
                it
        }
    }*/

        return res.sortedWith(sortEventsReadAsc())
    }


    private fun sortEventsReadAsc(): Comparator<Pair<EventReadODB, EventODB>> {
        return Comparator { o1, o2 ->
            if (o1.first.read != null && o2.first.read != null)
                o1.first.read!!.compareTo(o2.first.read)
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





