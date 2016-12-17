import {
    TaskDTOListForDay,
    createTaskDTOListKey,
    TaskDTO,
    TaskDTOState,
    TaskType,
    TaskStatus,
    TaskForDays,
    TaskProgressDTOListForDay,
    TaskDTOList,
    TaskProgressDTO
} from "./TaskDTO";
import {
    LOAD_TASKS_RESPONSE_OLDWAY,
    MARK_TASK_DONE_OPTIMISTIC,
    MODIFY_TASK_OPTIMISTIC,
    DELETE_TASK_OPTIMISTIC,
    MODIFY_TASK_REQUEST,
    TASK_PROGRESS_REQUEST,
    OPEN_EDIT_TASK,
    CLOSE_EDIT_TASK,
    CREATE_AND_OPEN_EDIT_TASK,
    CLOSE_TASK_OPTIMISTIC,
    MARK_ALL_CHALLENGE_TASKS_AS_INVALID,
    LOAD_TASK_PROGRESSES_RESPONSE,
    LOAD_TASKS_RESPONSE_NEWWAY
} from "./taskActionTypes";
import {isAction} from "../redux/ReduxTask";
import {DISPLAY_REQUEST_IN_PROGRESS} from "../redux/actions/actions";
import {WebState} from "../logic/domain/Common";
import * as path from "immutable-path";
import _ = require("lodash");
//import update from 'immutability-helper';

const getInitialState = (): TaskDTOState => {
    return {
        tasksForDays: {},//new Map<string,TaskDTOListForDay>(),
        taskProgressesForDays: {},
        allTasks: []
    };
}


export function tasksState(state: TaskDTOState = getInitialState(), action) {

    if (isAction(action, 'LOGOUT')) {
        return getInitialState();
    }
    if (isAction(action, CREATE_AND_OPEN_EDIT_TASK)) {
        var today = new Date();
        var taskDTO = {
            id: 0,
            icon: "fa-book",
            difficulty: 0,
            label: "Example task 1",
            taskType: TaskType.onetime,
            taskStatus: TaskStatus.waiting_for_acceptance,
            dueDate: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7).getTime(),
            userId: action.forUserId,
            challengeId: action.challengeId,
            done: false,
            createdByUserId: action.creatorId,
        };

        return Object.assign({}, state, {
            editedTask: taskDTO
        });
    } else if (isAction(action, OPEN_EDIT_TASK)) {
        var taskCopy: TaskDTO = Object.assign({}, action);
        return Object.assign({}, state, {
            editedTask: taskCopy
        })
    } else if (isAction(action, CLOSE_EDIT_TASK)) {
        return Object.assign({}, state, {
            editedTask: undefined
        })
    } else if (isAction(action, LOAD_TASKS_RESPONSE_OLDWAY)) {

        var key: string = createTaskDTOListKey(action.challengeId, action.day);
        return path.map(state, `tasksForDays.${key}`, ()=> action);

    } else if (isAction(action, LOAD_TASKS_RESPONSE_NEWWAY)) {
        var copy: TaskDTOList = Object.assign({}, state.allTasks, _.keyBy(action.tasks, 'id'));

        console.log("COPY");
        console.log(copy);

        // var bewState= path.map(state, `allTasks`, ()=> { copy });
        return Object.assign({}, state, {allTasks: copy})

    } else if (isAction(action, LOAD_TASK_PROGRESSES_RESPONSE)) {
        var tl: TaskProgressDTOListForDay = action.payload
        var key: string = createTaskDTOListKey(tl.challengeId, tl.day);

        var newState = state;
        var tss: TaskDTO[] = tl.taskProgresses.filter(tp=> tp.task != null).map(tp=>tp.task);
        if (tss.length > 0) {
            var copy: TaskDTOList = Object.assign({}, state.allTasks, _.keyBy(tss, 'id'));
            newState = Object.assign({}, newState, {allTasks: copy})
            // no need to store tasks here, so clear them
            tl.taskProgresses.map(tp=>tp.task = undefined);

        }
        return path.map(newState, `taskProgressesForDays.${key}`, ()=> tl);

    } else if (isAction(action, MARK_TASK_DONE_OPTIMISTIC)) {

        var key: string = createTaskDTOListKey(action.challengeId, new Date(action.taskProgress.progressTime));
        return path.map(state, `taskProgressesForDays.${key}`, (tl: TaskProgressDTO)=> {
            tl.done = action.taskProgress.done;
            return tl
        });

    } else if (isAction(action, DELETE_TASK_OPTIMISTIC)) {

        var copy: TaskDTOList = _.omit(state.allTasks, [action.id])
        var newState = Object.assign({}, state, {allTasks: copy})
        newState.editedTask = undefined;
        return newState;

    } else {


        var newTasks = tasks(state.tasksForDays, action);
        if (newTasks != state.tasksForDays) {
            return Object.assign({}, state, {
                tasksForDays: newTasks
            })
        } else return state;


    }
}


function tasks(state: TaskForDays, action) {
    if (isAction(action, MARK_ALL_CHALLENGE_TASKS_AS_INVALID)) {
        console.log("MARK ALL AS INVALID");


        var newState: TaskForDays = Object.assign({});
        $.map(state, (taskListForDayDTO: TaskDTOListForDay, key: string)=> {
            if (taskListForDayDTO.challengeId == action.challengeId) {
                newState[key] = Object.assign({}, taskListForDayDTO, {webState: WebState.NEED_REFETCH});
            } else newState[key] = taskListForDayDTO;

        })
        return newState;

    } else if (isAction(action, LOAD_TASKS_RESPONSE_OLDWAY)) {
        var key: string = createTaskDTOListKey(action.challengeId, action.day);
        var newState: TaskForDays = Object.assign({}, state, {[key]: action});


        return newState;
    } else if (isAction(action, MODIFY_TASK_OPTIMISTIC)) {

        var taskCopy: TaskDTO = Object.assign({}, action);
        var newState: TaskForDays = Object.assign({}, state);
        markTaskListInvalid(newState, action.id);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            taskListForDayDTO.taskList = taskListForDayDTO.taskList.map((t: TaskDTO)=>t.id != action.id ? t : taskCopy);
        });
        return newState;
    } else if (isAction(action, CLOSE_TASK_OPTIMISTIC)) {

        var newState: TaskForDays = Object.assign({}, state);
        markTaskListInvalid(newState, action.task.id);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            taskListForDayDTO.taskList = taskListForDayDTO.taskList.map((t: TaskDTO)=>t.id != action.task.id ? t : Object.assign({}, action.task, {done: t.done}));
        });
        return newState;
    } else if (isAction(action, DISPLAY_REQUEST_IN_PROGRESS)) {
        var newState: TaskForDays = Object.assign({}, state);

        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            if (taskListForDayDTO.webState == WebState.FETCHING /*|| taskListForDayDTO.webState == WebState.NEED_REFETCH */) {
                console.log("DISPLAY REQUEST IN PROGRESS: FETCHING => FETCHING VISIBLE " + taskListForDayDTO.day);
                taskListForDayDTO.webState = WebState.FETCHING_VISIBLE;
            }
        });
        return newState;
    } else if (isAction(action, MODIFY_TASK_REQUEST)) {
        var newState: TaskForDays = Object.assign({}, state);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            if (taskListForDayDTO.webState == WebState.NEED_REFETCH)
                taskListForDayDTO.webState = WebState.FETCHING;
        });
        return newState;
    } else if (isAction(action, TASK_PROGRESS_REQUEST)) {
        var key: string = createTaskDTOListKey(action.challengeId, new Date(action.taskProgress.progressTime));

        var newState: TaskForDays = Object.assign({}, state);
        var taskList: TaskDTOListForDay = newState[key];
        if (taskList.webState == WebState.NEED_REFETCH)
            taskList.webState = WebState.FETCHING;
        return newState;
    }
    else return state;
}

function markTaskDayInvalid(taskListForDayDTO: TaskDTOListForDay, taskId: number) {
    if (taskListForDayDTO.taskList.map(t=>t.id).contains(taskId)) {
        taskListForDayDTO.webState = WebState.NEED_REFETCH;
        taskListForDayDTO.invalidTasksIds.push(taskId);
    }
}
function markTaskListInvalid(state: TaskForDays, taskId: number) {
    $.map(state, (taskListForDayDTO: TaskDTOListForDay)=> {
        markTaskDayInvalid(taskListForDayDTO, taskId);
    })
}
function markTaskListInvalidAll(state: TaskForDays) {
    $.map(state, (taskListForDayDTO: TaskDTOListForDay)=> {
        taskListForDayDTO.webState = WebState.NEED_REFETCH;
    })
}