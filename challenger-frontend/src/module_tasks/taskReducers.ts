import {TaskDTOListForDay, createTaskDTOListKey, TaskDTO, TaskDTOState, TaskType, TaskStatus} from "./TaskDTO";
import {
    LOAD_TASKS_RESPONSE, MARK_TASK_DONE_OPTIMISTIC, MODIFY_TASK_OPTIMISTIC, DELETE_TASK_OPTIMISTIC, MODIFY_TASK_REQUEST, TASK_PROGRESS_REQUEST, OPEN_EDIT_TASK, CLOSE_EDIT_TASK,
    CREATE_AND_OPEN_EDIT_TASK
} from "./taskActionTypes";
import {isAction, Action} from "../redux/ReduxTask";
import {DISPLAY_REQUEST_IN_PROGRESS} from "../redux/actions/actions";
import {WebState} from "../logic/domain/Common";


const getInitialState = (): TaskDTOState => {
    return {tasks: new Map<string,TaskDTOListForDay>()};
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
            tasks: tasks(state.tasks, taskDTO),
            editedTask: taskDTO
        });
    }
    else if (isAction(action, OPEN_EDIT_TASK)) {
        var taskCopy: TaskDTO = Object.assign({}, action as TaskDTO);
        return Object.assign({}, state, {
            tasks: tasks(state.tasks, action),
            editedTask: taskCopy
        })
    } else if (isAction(action, CLOSE_EDIT_TASK)) {
        return Object.assign({}, state, {
            tasks: tasks(state.tasks, action),
            editedTask: undefined
        })
    } else if (isAction(action, DELETE_TASK_OPTIMISTIC)) {
        return Object.assign({}, state, {
            tasks: tasks(state.tasks, action),
            editedTask: undefined
        })
    } else {
        var newTasks = tasks(state.tasks, action);
        if (newTasks != state.tasks) {
            return Object.assign({}, state, {
                tasks: newTasks
            })
        } else return state;


    }
}

function tasks(state: Map<string,TaskDTOListForDay>, action) {

    if (isAction(action, LOAD_TASKS_RESPONSE)) {


        var key: string = createTaskDTOListKey(action.challengeId, action.day);

        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);


        var newAction: TaskDTOListForDay = Object.assign({}, action);
        newState[key] = Object.assign({}, action);



        return newState;
    } else if (isAction(action, MARK_TASK_DONE_OPTIMISTIC)) {
        var key: string = createTaskDTOListKey(action.challengeId, new Date(action.taskProgress.progressTime));
        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);
        var task: TaskDTO = newState[key].taskList.find((t: TaskDTO)=>t.id == action.taskProgress.taskId);
        task.done = action.taskProgress.done;
        markTaskDayInvalid(newState[key], action.taskProgress.taskId);
        return newState;
    } else if (isAction(action, MODIFY_TASK_OPTIMISTIC)) {

        var taskCopy: TaskDTO = Object.assign({}, action);
        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);
        markTaskListInvalid(newState, action.id);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            taskListForDayDTO.taskList = taskListForDayDTO.taskList.map((t: TaskDTO)=>t.id != action.id ? t : taskCopy);
        });
        return newState;
    } else if (isAction(action, DELETE_TASK_OPTIMISTIC)) {
        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);
        markTaskListInvalid(newState, action.id);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            taskListForDayDTO.taskList = taskListForDayDTO.taskList.filter((t: TaskDTO)=>t.id != action.id);
        });

        return newState;
    } else if (isAction(action, DISPLAY_REQUEST_IN_PROGRESS)) {
        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);

        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            if (taskListForDayDTO.webState == WebState.FETCHING || taskListForDayDTO.webState == WebState.NEED_REFETCH)
                taskListForDayDTO.webState = WebState.FETCHING_VISIBLE;
        });
        return newState;
    } else if (isAction(action, MODIFY_TASK_REQUEST)) {
        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            if (taskListForDayDTO.webState == WebState.NEED_REFETCH)
                taskListForDayDTO.webState = WebState.FETCHING;
        });
        return newState;
    } else if (isAction(action, TASK_PROGRESS_REQUEST)) {
        var key: string = createTaskDTOListKey(action.challengeId, new Date(action.taskProgress.progressTime));

        var newState: Map<string,TaskDTOListForDay> = Object.assign({}, state);
        $.map(newState, (taskListForDayDTO: TaskDTOListForDay)=> {
            var task: TaskDTO = newState[key].taskList.filter((t: TaskDTO)=>t.id == action.taskProgress.taskId).pop();
            if (taskListForDayDTO.webState == WebState.NEED_REFETCH)
                taskListForDayDTO.webState = WebState.FETCHING;
        });
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
function markTaskListInvalid(state: Map<string,TaskDTOListForDay>, taskId: number) {
    $.map(state, (taskListForDayDTO: TaskDTOListForDay)=> {
        markTaskDayInvalid(taskListForDayDTO, taskId);
    })
}