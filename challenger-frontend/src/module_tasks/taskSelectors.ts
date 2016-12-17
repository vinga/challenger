import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {createTaskDTOListKey, TaskDTO, TaskDTOListForDay, TaskStatus, TaskDTOList, TaskProgressForDay, TaskProgressesForDays, TaskProgressDTOListForDay} from "./TaskDTO";
import {WebState} from "../logic/domain/Common";




const userDynamicSelector = (state:ReduxState, userId: number):number => {
    return userId
}
/*const taskDynamicSelector = (state:ReduxState, tasks: TaskDTO[]):TaskDTO[] => {
    return tasks
}*/

export const allTasksSelector: Selector<ReduxState, TaskDTOList> = (state, map): TaskDTOList => state.tasksState.allTasks;

export const currentTaskProgressSelector: Selector<ReduxState, TaskProgressDTOListForDay> = (state, map): TaskProgressDTOListForDay => {
    var key=createTaskDTOListKey(state.challenges.selectedChallengeId, state.currentSelection.day);
    return state.tasksState.taskProgressesForDays[key];
}

function calculateTasksForUserAndDayHelper(taskProgressForDay: TaskProgressDTOListForDay, tasks: TaskDTOList, userId: number) {
    var ret;
    if (taskProgressForDay == null)
        ret = [];
    else ret = taskProgressForDay.taskProgresses.map(tp=> {
        var task = tasks[tp.taskId]
        if (task == null || task.userId != userId)
            return null;
        return Object.assign({}, task, {done: tp.done});
    }).filter(t=>t != null)
    return ret;
}
export const makeGetTasksForUserAndDay = () => {
    return createSelector(
        currentTaskProgressSelector,
        allTasksSelector,
        userDynamicSelector ,
        (taskProgressForDay: TaskProgressDTOListForDay, tasks:TaskDTOList,  userId: number): Array<TaskDTO> => {
return calculateTasksForUserAndDayHelper(taskProgressForDay, tasks, userId);

        }
    );
}



export const makeBusyTasksSelectorForUserAndDay = () => {
    return createSelector(
        currentTaskProgressSelector,
       /* taskDTOListKeySelector,
        taskDynamicSelector ,*/
        (tp: TaskProgressDTOListForDay /*taskDtoForDay: TaskDTOListForDay, tasksLists: TaskDTO[]*/): boolean => {
            if (tp==null)
                return false;
            var busy = tp.webState == WebState.FETCHING_VISIBLE;
            return busy;

        /*    if (taskDtoForDay==null)
                return false;
            var busy = taskDtoForDay.webState == WebState.FETCHING_VISIBLE;
            if (busy) {
                busy = tasksLists.filter((t: TaskDTO)=>taskDtoForDay.invalidTasksIds.contains(t.id)).length > 0;
            }
            return busy;*/
        }
    );
}

export const makeCalculateAllAndCheckedCount = () => {

    return createSelector(
        currentTaskProgressSelector,
        allTasksSelector,
        userDynamicSelector ,
        (taskProgressForDay: TaskProgressDTOListForDay, tasks:TaskDTOList,  userId: number): {checkedPoints:number, allPoints:number} => {



            var userDayTasks= calculateTasksForUserAndDayHelper(taskProgressForDay, tasks, userId);

            var checkedPoints=userDayTasks.filter(t=>t.taskStatus == TaskStatus.accepted && t.done)
                .map(t=>t.difficulty + 1)
                .reduce((total, num)=>total + num, 0);
            var allPoints= userDayTasks
                .map(t=>t.difficulty + 1)
                .reduce((total, num)=>total + num, 0);

            return {checkedPoints, allPoints}

        }
    );
}

