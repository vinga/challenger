import {TaskDTO, TaskProgressDTO, TaskApprovalDTO, createTaskDTOListKey, TaskDTOListForDay, TaskUserDTO} from "./TaskDTO";
import {ReduxState} from "../redux/ReduxState";
import {MODIFY_TASK_OPTIMISTIC, MODIFY_TASK_REQUEST, DELETE_TASK_OPTIMISTIC, MARK_TASK_DONE_OPTIMISTIC, TASK_PROGRESS_REQUEST, LOAD_TASKS_REQUEST, LOAD_TASKS_RESPONSE} from "./taskActionTypes";
import {DISPLAY_REQUEST_IN_PROGRESS} from "../redux/actions/actions";
import {WebState} from "../logic/domain/Common";
import {selectedChallengeParticipantIds} from "../module_challenges/index";
import * as webCall from "./taskWebCalls";


export function updateTask(task: TaskDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(MODIFY_TASK_OPTIMISTIC.new(task));
        dispatch(MODIFY_TASK_REQUEST.new(task));
        if (task.id <= 0) {
            webCall.createTask(task)
                .then((task: TaskDTO)=> {
                    dispatch(fetchTasks(getState().challenges.selectedChallengeId, getState().currentSelection.day));
                });
        } else {
            webCall.updateTask(task)
                .then((task: TaskDTO)=> {
                    dispatch(fetchTasks(getState().challenges.selectedChallengeId, getState().currentSelection.day));
                });
        }
        dispatch(displayInProgressWebRequestsIfAny());
    }
}

export function deleteTask(task: TaskDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        task.deleted = true;
        dispatch(DELETE_TASK_OPTIMISTIC.new(task));
        dispatch(MODIFY_TASK_REQUEST.new(task));
        webCall.deleteTask(task).then(()=> {
            dispatch(fetchTasks(getState().challenges.selectedChallengeId, getState().currentSelection.day));
        });
        dispatch(displayInProgressWebRequestsIfAny());
    }
}

function displayInProgressWebRequestsIfAny() {
    return function (dispatch, getState: ()=>ReduxState) {
        setTimeout(() => {
            dispatch(DISPLAY_REQUEST_IN_PROGRESS.new({}));
        }, 500); // after half sec
    }
}

export function markTaskDoneOrUndone(caller: TaskUserDTO, challengeId: number, taskProgress: TaskProgressDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        var state: ReduxState = getState();
        var key: string = createTaskDTOListKey(challengeId, new Date(taskProgress.progressTime));



        var task: TaskDTOListForDay = getState().tasksState.tasks[key];
        dispatch(MARK_TASK_DONE_OPTIMISTIC.new({challengeId, taskProgress}));
        dispatch(TASK_PROGRESS_REQUEST.new({challengeId, taskProgress}));

        webCall.updateTaskProgress(challengeId, taskProgress, caller.jwtToken)
            .then((tpRes: TaskProgressDTO)=> {
                // tpRes caused wrong date (previous day), cause response had zeroed hour
               dispatch(fetchTasks(challengeId, new Date(taskProgress.progressTime)));
            });

        dispatch(displayInProgressWebRequestsIfAny());

    }
}


export function fetchTasks(challengeId: number, day: Date): any {
    return function (dispatch) {
        dispatch(LOAD_TASKS_REQUEST.new({challengeId, day}));
        var key: string = createTaskDTOListKey(challengeId, day);

        webCall.loadTasks(challengeId, day).then(
            (taskList: Array<TaskDTO>)=> {
                var key: string = createTaskDTOListKey(challengeId, day);
                dispatch(LOAD_TASKS_RESPONSE.new({taskList: taskList, challengeId: challengeId, day: day, lastUpdated: new Date(), webState: WebState.FETCHED, invalidTasksIds: []}))
            }
        );
    }
};

export function fetchTasksWhenNeeded(challengeId: number, day: Date): any {
    return function (dispatch, getState: ()=>ReduxState) {
        var key: string = createTaskDTOListKey(challengeId, day);
        if (getState().tasksState.tasks[key] == null || getState().tasksState.tasks[key].webState == WebState.NEED_REFETCH) {
            dispatch(fetchTasks(challengeId, day));
        }
    }
}

export function updateTaskStatus(challengeId: number, taskApproval: TaskApprovalDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        // all other users should
        // user created task for himself, all others should accept it
        var challengeParticipantIds = selectedChallengeParticipantIds(getState());

        //TODO accounts selector
        var jwtTokensOfApprovingUsers: Array<String> = getState().accounts.filter(a=>a.jwtToken != null &&
        challengeParticipantIds.contains(a.userId))
            .map(a=>a.jwtToken);
        //TODO optimistic update
        /*        if (jwtTokensOfApprovingUsers.length==challengeParticipantIds.length-1) {
         getState().tasks.
         task.taskStatus=TaskStatus.accepted;
         dispatch(MODIFY_TASK_OPTIMISTIC.new(task));
         dispatch(MODIFY_TASK_REQUEST.new(task));
         }*/

        webCall.updateTaskStatus(challengeId, taskApproval, jwtTokensOfApprovingUsers)
            .then((task: TaskDTO)=> {
                dispatch(fetchTasks(getState().challenges.selectedChallengeId, getState().currentSelection.day));
            });
        dispatch(displayInProgressWebRequestsIfAny());
    }
}


