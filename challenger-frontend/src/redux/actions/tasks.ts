import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import {TaskDTO} from "../../logic/domain/TaskDTO";

export const LOAD_TASKS_REQUEST='LOAD_TASKS_REQUEST';
export const LOAD_TASKS_RESPONSE='LOAD_TASKS_RESPONSE';


function loadTasksRequest(challengeId: number, day: Date) {
    return { type: LOAD_TASKS_REQUEST, challengeId: challengeId, day: day }
}
function loadTasksResponse(taskList: Array<TaskDTO>, challengeId:number, day: Date) {
    return { type: LOAD_TASKS_RESPONSE, taskList: taskList, challengeId: challengeId, day: day }
}

export function fetchTasks(challengeId: number, day: Date): any {
    return function (dispatch) {
        dispatch(loadTasksRequest(challengeId, day));
        ajaxWrapper.loadTasks(challengeId, day).then(
            taskList=>
                dispatch(loadTasksResponse(taskList, challengeId, day))
        );
    }
};

export function fetchTasksWhenNeeded(challengeId: number,  day: Date): any {
    return function (dispatch, getState) {
        if (getState().tasks["" + challengeId + "-" + day.toISOString().slice(0, 10)]==null) {
            dispatch(fetchTasks(challengeId,day));
        }
    }
}