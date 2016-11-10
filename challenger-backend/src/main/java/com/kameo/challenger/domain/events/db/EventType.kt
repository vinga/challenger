package com.kameo.challenger.domain.events.db

/**
 * Created by Kamila on 2016-10-23.
 */
enum class EventType {
    POST,
    CREATE_TASK,
    UPDATE_TASK,
    ACCEPT_TASK,
    REJECT_TASK,
    CHECKED_TASK,
    UNCHECKED_TASK,
    DELETE_TASK
}