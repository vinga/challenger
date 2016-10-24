package com.kameo.challenger.domain.accounts

import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.events.EventODB
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.utils.odb.AnyDAONew
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.inject.Inject

@Component
@Transactional
open class EventGroupDAO(@Inject val anyDaoNew: AnyDAONew) {

    open fun getPostsForTask(callerId: Long, taskId: Long): List<EventODB> {
        checkHasPermissionToTask(callerId,taskId);

        return anyDaoNew.getAll(EventODB::class, {
            it get EventODB::task eqId taskId
        })
    }

    open fun getPostsForChallenge(callerId: Long, challengeId: Long, maxPosts:Int=10): List<EventODB> {
        checkHasPermissionToChallenge(callerId,challengeId);

        return anyDaoNew.getAll(EventODB::class, {
            it get EventODB::challenge eqId challengeId

            it orderByDesc EventODB::createDate

        }, maxPosts)
    }

    open fun editPost(callerId: Long, p: EventODB):EventODB {
        checkHasPermissionToChallenge(callerId,p.challenge.id);


        val task=p.task ?: null;
        if (task!=null) {
            // make sure task & challenge id are correct
            anyDaoNew.getOne(TaskODB::class, {
                it eqId task.id
                it get TaskODB::challenge eqId p.challenge.id
            })
        }
        if (p.isNew()) {
            anyDaoNew.em.persist(p);
        } else {
            anyDaoNew.em.merge(p);
        }
        return p;
    }

    private fun checkHasPermissionToTask(callerId: Long, taskId: Long) {
        val exists= anyDaoNew.exists(TaskODB::class, {
            it eqId taskId
            it.join(TaskODB::challenge).joinList(ChallengeODB::participants)
                    .eqId(callerId)
                    .get(ChallengeParticipantODB::challengeStatus) eq ChallengeStatus.ACTIVE

        })
        if (!exists)
            throw IllegalArgumentException("No permission");
    }

    private fun checkHasPermissionToChallenge(callerId: Long, challengeId: Long) {
        val exists= anyDaoNew.exists(ChallengeParticipantODB::class, {
            it get ChallengeParticipantODB::user eqId callerId
            it get ChallengeParticipantODB::challenge eqId  challengeId
            it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
        })
        if (!exists)
            throw IllegalArgumentException("No permission");
    }

    fun  getChallengeIdForTaskId(callerId: Long, taskId: Long): Long {
        checkHasPermissionToTask(callerId, taskId);
        return anyDaoNew.getOne(TaskODB::class,{it eqId taskId}).challenge.id;
    }
}


