import {isAction, Action} from "../ReduxTask";
import {TaskDTO, TaskDTOListForDay, createTaskDTOListKey} from "../../logic/domain/TaskDTO";
import {
    LOAD_TASKS_RESPONSE, MARK_TASK_DONE_OPTIMISTIC, MODIFY_TASK_OPTIMISTIC, DELETE_TASK_OPTIMISTIC,
    DISPLAY_REQUEST_IN_PROGRESS, MODIFY_TASK_REQUEST, TASK_PROGRESS_REQUEST
} from "../actions/actions";
import {WebState} from "../../logic/domain/Common";


export default function tasks(state:Map<string,TaskDTOListForDay> = new Map<string,TaskDTOListForDay>(), action:Action) {
    if (isAction(action, LOAD_TASKS_RESPONSE)) {
        var key:string = createTaskDTOListKey(action.challengeId, action.day);
        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state)
        var newAction:TaskDTOListForDay = Object.assign({}, action);
        newState[key] = Object.assign({}, action);
        console.log(newAction);
        return newState;
    } else if (isAction(action, MARK_TASK_DONE_OPTIMISTIC)) {
        var key:string = createTaskDTOListKey(action.challengeId, new Date(action.taskProgress.progressTime));
        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state)
        var task:TaskDTO = newState[key].taskList.filter((t:TaskDTO)=>t.id == action.taskProgress.taskId).pop();
        task.done = action.taskProgress.done;
        markTaskDayInvalid(newState[key], action.taskProgress.taskId);
        return newState;
    } else if (isAction(action, MODIFY_TASK_OPTIMISTIC)) {

        var taskCopy:TaskDTO = Object.assign({}, action);
        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state);
        markTaskListInvalid(newState, action.id);
        $.map(newState, (taskListForDayDTO:TaskDTOListForDay)=> {
            taskListForDayDTO.taskList = taskListForDayDTO.taskList.map((t:TaskDTO)=>t.id != action.id ? t : taskCopy);
        })
        return newState;
    } else if (isAction(action, DELETE_TASK_OPTIMISTIC)) {
        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state);
        markTaskListInvalid(newState, action.id);
        $.map(newState, (taskListForDayDTO:TaskDTOListForDay)=> {
            taskListForDayDTO.taskList = taskListForDayDTO.taskList.filter((t:TaskDTO)=>t.id != action.id);
        });

        return newState;
    } else if (isAction(action,DISPLAY_REQUEST_IN_PROGRESS)) {
        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state);
        $.map(newState, (taskListForDayDTO:TaskDTOListForDay)=> {
            if (taskListForDayDTO.webState==WebState.FETCHING || taskListForDayDTO.webState==WebState.NEED_REFETCH)
                taskListForDayDTO.webState=WebState.FETCHING_VISIBLE;
        });
        return newState;
    } else if (isAction(action,MODIFY_TASK_REQUEST)) {
        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state);
        $.map(newState, (taskListForDayDTO:TaskDTOListForDay)=> {
            if (taskListForDayDTO.webState==WebState.NEED_REFETCH)
                taskListForDayDTO.webState=WebState.FETCHING;
        });
        return newState;
    } else if (isAction(action,TASK_PROGRESS_REQUEST)) {
        var key:string = createTaskDTOListKey(action.challengeId, new Date(action.taskProgress.progressTime));

        var newState:Map<string,TaskDTOListForDay> = Object.assign({}, state);
        $.map(newState, (taskListForDayDTO:TaskDTOListForDay)=> {
            var task:TaskDTO = newState[key].taskList.filter((t:TaskDTO)=>t.id == action.taskProgress.taskId).pop();
            if (taskListForDayDTO.webState==WebState.NEED_REFETCH)
                taskListForDayDTO.webState=WebState.FETCHING;
        });
        return newState;
    }
    else return state;
}

function markTaskDayInvalid(taskListForDayDTO:TaskDTOListForDay, taskId:number) {
    if (taskListForDayDTO.taskList.map(t=>t.id).contains(taskId)) {
        taskListForDayDTO.webState = WebState.NEED_REFETCH;
        taskListForDayDTO.invalidTasksIds.push(taskId);
    }
}
function markTaskListInvalid(state:Map<string,TaskDTOListForDay>, taskId:number) {
    $.map(state, (taskListForDayDTO:TaskDTOListForDay)=> {
        markTaskDayInvalid(taskListForDayDTO, taskId);
    })
}