import {Action, isAction} from "../ReduxTask";
import {
    INCREMENT_DAY, CHANGE_CHALLENGE, OPEN_EDIT_TASK, CLOSE_EDIT_TASK, DELETE_TASK_OPTIMISTIC,
    LOGIN_USER_RESPONSE_SUCCESS, LOAD_CONVERSATION_RESPONSE
} from "../actions/actions";
import {CurrentSelection} from "../ReduxState";
import {TaskDTO} from "../../logic/domain/TaskDTO";
import jwtDecode = require("jwt-decode");
import {ConversationDTO} from "../../logic/domain/ConversationDTO";


export default function currentSelection(state:CurrentSelection = { day: new Date() }, action: Action) {
    if (isAction(action, INCREMENT_DAY)) {
        return Object.assign({}, state, {
            day: state.day.addDays(action.amount)
        })
    } else if (isAction(action, CHANGE_CHALLENGE)) {
        return Object.assign({}, state, {
            challengeId: action.challengeId
        })
    } else if (isAction(action, OPEN_EDIT_TASK)) {
        var taskCopy:TaskDTO=Object.assign({}, action);
        return Object.assign({}, state, {
            editedTask: taskCopy
        })
    } else if (isAction(action, CLOSE_EDIT_TASK)) {
        return Object.assign({}, state, {
            editedTask: undefined
        })
    } else if (isAction(action, DELETE_TASK_OPTIMISTIC)) {
        return Object.assign({}, state, {
            editedTask: undefined
        })
    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return Object.assign({}, state, {
            userId: jwtDecode(action.jwtToken).info.userId
        })

    } else if (isAction(action, LOAD_CONVERSATION_RESPONSE)) {
        return Object.assign({}, state, {
            displayedConversation: action
        })

    }
    return state;
}
