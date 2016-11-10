package com.kameo.challenger.domain.events

import com.kameo.challenger.domain.challenges.IChallengeRestService
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.events.db.EventODB
import com.kameo.challenger.domain.events.db.EventReadODB
import com.kameo.challenger.domain.events.db.EventType
import lombok.Data
import javax.ws.rs.PathParam

/**
 * Created by Kamila on 2016-10-21.
 */
interface IEventGroupRestService {

    fun getEventsForTask(challengeId: Long, taskId: Long): EventGroupDTO

    fun getEventsForChallenge(challengeId: Long): EventGroupDTO

    fun createEvent(challengeId: Long, eventDTO: EventDTO): EventDTO

    data class EventGroupDTO(val challengeId: Long, val taskId: Long? = null, val posts: Array<EventDTO> = emptyArray())  {


    }



    data class EventDTO(val id: Long=0,
                        var authorId: Long = 0,
                        var content: String = "",
                        var sentDate: Long = 0,
                        var taskId: Long? = null,
                        var challengeId: Long =0,
                        var eventType: EventType = EventType.POST,
                        var readDate: Long?=null,
                        var eventReadId: Long=0 // used for sorting if readDate is null
                        ) {





        companion object {
            fun fromODB(pair : Pair<EventReadODB, EventODB>): EventDTO {
                val co = fromODB(pair.second);
                co.readDate=pair.first.read?.time ?: null
                co.eventReadId=pair.first.id
                return co
            }
            fun fromODB(c: EventODB): EventDTO {
                val co = EventDTO(id = c.id,
                        authorId = c.author.id,
                        content = c.content,
                        sentDate = c.createDate.time,
                        taskId = c.task?.id ?: null,
                        challengeId = c.challenge.id,
                        eventType = c.eventType
                );
                return co
            }
        }
    }
}