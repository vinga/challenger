package com.kameo.challenger.domain.events

import com.kameo.challenger.domain.events.db.EventODB
import com.kameo.challenger.domain.events.db.EventReadODB
import com.kameo.challenger.domain.events.db.EventType
import java.time.ZoneOffset


interface IEventGroupRestService {


    fun getEventsForChallenge(challengeId: Long, beforeEventReadId: Long?, max: Int?): EventGroupSynchDTO

    fun createEvent(challengeId: Long, eventDTO: EventDTO): EventDTO

    data class EventGroupDTO(
            val events: Array<EventDTO> = emptyArray(),
            val maxTotalEventReadId: Long?=null
    )
    data class EventGroupSynchDTO(
            val challengeId: Long,
            val taskId: Long? = null,
            val events: Array<EventDTO> = emptyArray(),
            val canBeMore: Boolean?=null // used in sync fetching events, to indicate that there are previous posts
    )


    data class EventDTO(val id: Long = 0,
                        var authorId: Long = 0,
                        var content: String = "",
                        var sentDate: Long = 0,
                        var forDay: Long = 0,
                        var taskId: Long? = null,
                        var challengeId: Long = 0,
                        var eventType: EventType = EventType.POST,
                        var readDate: Long? = null,
                        var eventReadId: Long = 0, // used for sorting if readDate is null,
                        var affectedUserId: Long? = null

    ) {


        companion object {
            fun fromODB(pair: Pair<EventReadODB, EventODB>): EventDTO {
                val co = fromODB(pair.second)
                co.readDate = pair.first.read?.time
                co.eventReadId = pair.first.id
                return co
            }

            fun fromODB(c: EventODB): EventDTO {
                val co = EventDTO(id = c.id,
                        authorId = c.author.id,
                        content = c.content,
                        sentDate = c.createDate.time,
                        forDay = c.forDay.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                        taskId = c.taskId,
                        challengeId = c.challenge.id,
                        eventType = c.eventType,
                        affectedUserId = c.affectedUser?.id
                )
                return co
            }
        }


    }
}