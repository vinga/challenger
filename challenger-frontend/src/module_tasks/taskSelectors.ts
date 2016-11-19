import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {createTaskDTOListKey, TaskDTO, TaskDTOListForDay, TaskStatus} from "./TaskDTO";
import {WebState} from "../logic/domain/Common";



export const taskDTOListKeySelector = (state:ReduxState):TaskDTOListForDay => {
    var key=createTaskDTOListKey(state.challenges.selectedChallengeId, state.currentSelection.day);
    return state.tasksState.tasks[key];
}
const userDynamicSelector = (state:ReduxState, userId: number):number => {
    return userId
}
const taskDynamicSelector = (state:ReduxState, tasks: TaskDTO[]):TaskDTO[] => {
    return tasks
}


export const makeGetTasksForUserAndDay = () => {
    return createSelector(
         taskDTOListKeySelector, userDynamicSelector ,
        (taskDtoForDay: TaskDTOListForDay, userId: number): Array<TaskDTO> => {
            if (taskDtoForDay==null)
                return [];
            else
                return taskDtoForDay.taskList.filter(ta=>ta.userId==userId)
        }
    );
}

export const makeBusyTasksSelectorForUserAndDay = () => {
    return createSelector(
        taskDTOListKeySelector,
        taskDynamicSelector ,
        (taskDtoForDay: TaskDTOListForDay, tasksLists: TaskDTO[]): boolean => {


            if (taskDtoForDay==null)
                return false;
            var busy = taskDtoForDay.webState == WebState.FETCHING_VISIBLE;
            if (busy) {
                busy = tasksLists.filter((t: TaskDTO)=>taskDtoForDay.invalidTasksIds.contains(t.id)).length > 0;
            }
            return busy;
        }
    );
}

export const makeCalculateAllAndCheckedCount = () => {
    return createSelector(
        taskDTOListKeySelector, userDynamicSelector ,
        (taskDtoForDay: TaskDTOListForDay, userId: number): {allPoints: number, checkedPoints: number} => {

            var tasks= (taskDtoForDay==null)? []: taskDtoForDay.taskList.filter(ta=>ta.userId==userId);

            var checkedPoints=tasks.filter(t=>t.taskStatus == TaskStatus.accepted && t.done)
                .map(t=>t.difficulty + 1)
                .reduce((total, num)=>total + num, 0);
            var allPoints= tasks
                .map(t=>t.difficulty + 1)
                .reduce((total, num)=>total + num, 0);

            return {checkedPoints, allPoints}

        }
    );
}

