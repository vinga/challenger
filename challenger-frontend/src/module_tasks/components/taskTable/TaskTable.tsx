import * as React from "react";
import {ReduxState, connect} from "../../../redux/ReduxState";
import {Table, TableBody, TableRow, TableRowColumn} from "material-ui/Table";
import Paper from "material-ui/Paper";
import DifficultyIconButton from "./DifficultyIconButton.tsx";
import ChallengeTableCheckbox from "./ChallengeTableCheckbox.tsx";
import {TaskDTO, TaskProgressDTO, TaskDTOListForDay, TaskUserDTO} from "../../TaskDTO";
import {markTaskDoneOrUndone} from "../../taskActions";
import {OPEN_EDIT_TASK} from "../../taskActionTypes";
import {ResizeAware} from "../../../views/Constants";
import {TaskLabel} from "../TaskLabel";
import {TaskTableHeader} from "./TaskTableHeader";
import {SHOW_TASK_EVENTS} from "../../../module_events/eventActionTypes";
import {taskDTOListKeySelector, makeGetTasksForUserAndDay, makeBusyTasksSelectorForUserAndDay} from "../../taskSelectors";

const styles = {
    icon: {
        width: '50px',
        padding: '0px'
    },
    label: {
        padding: '5px'
    },
    taskType: {
        width: '40px',
        padding: '0px',
        color: 'grey',
        fontSize: '11px'
    },

};
interface Props {
    challengeId: number
    user: TaskUserDTO,
    userIsAuthorized: boolean,
    no: number,
    showAuthorizeFuncIfNeeded: (eventTarget: EventTarget, userId: number)=>JQueryPromise<boolean>
}

interface ReduxProps {
    currentDate: Date,
    tasksList: Array<TaskDTO>,
    busy: boolean,
}


interface ReduxPropsFunc {
    onTaskCheckedStateChangedFunc: (caller: TaskUserDTO, challengeId: number, taskProgress: TaskProgressDTO)=>void;
    onEditTask: (task: TaskDTO)=>void;
    onShowTaskEvents: (task: TaskDTO, no: number) => void;

}

class TaskTableInternal extends React.Component<Props & ReduxProps & ReduxPropsFunc, void> {


    handleResize = (e) => {
        this.forceUpdate();
    };

    onTaskCheckedStateChangedFunc = (taskDTO) => {
        var taskProgressDTO = {
            taskId: taskDTO.id,
            done: taskDTO.done,
            progressTime: this.props.currentDate.getTime()
        };
        this.props.onTaskCheckedStateChangedFunc(this.props.user, this.props.challengeId, taskProgressDTO);
    };


    render() {
        var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 400) + "px";
        var other = {minHeight: height, height: height, overflowY: "auto", overflowX: "none"};

        //TODO delay wyszarzenie jesli call nie wrocil
        if (this.props.busy) {
            Object.assign(other, {opacity: "0.4"});
        }

        return (<Paper style={{padding: '10px', display: "inline-block"}}>
                    <div style={other}>
                        <Table selectable={false}
                               fixedHeader={true}
                        >
                            <TableBody displayRowCheckbox={false}>
                                { this.props.tasksList.map(task =>
                                    <TableRow key={task.id}>
                                        <TableRowColumn style={styles.icon}>
                                            <DifficultyIconButton
                                                no={this.props.no}
                                                task={task}
                                                onEditTask={this.props.onEditTask}
                                                onShowTaskEvents={this.props.onShowTaskEvents}
                                                showTooltip={true}
                                            />
                                        </TableRowColumn>
                                        <TableRowColumn style={styles.label}>
                                            <TaskLabel
                                                no={this.props.no}
                                                taskDTO={task}
                                                user={this.props.user}
                                            />
                                        </TableRowColumn>
                                        <TableRowColumn style={styles.taskType}>
                                            {task.taskType}
                                        </TableRowColumn>
                                        <TableRowColumn style={{width: '45px', padding: '10px'}}>
                                            <ChallengeTableCheckbox
                                                no={this.props.no}
                                                userId={this.props.user.id}
                                                taskDTO={task}
                                                showAuthorizeFuncIfNeeded={this.props.showAuthorizeFuncIfNeeded}
                                                onTaskCheckedStateChangedFunc={this.onTaskCheckedStateChangedFunc}
                                                authorized={this.props.userIsAuthorized}
                                            />
                                        </TableRowColumn>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </div>
                </Paper>

        );
    }
}
const mapStateToProps = () => {
    // component instance selectors
    const tasksForUserAndDay = makeGetTasksForUserAndDay();
    const busyTasksSelectorForUserAndDay = makeBusyTasksSelectorForUserAndDay();


    // real mapStateToProps function
    const mapStateToPropsInternal = (state: ReduxState, ownprops: Props): ReduxProps => {

        var tasksLists=tasksForUserAndDay(state, ownprops.user.id);
        return {
            currentDate: state.currentSelection.day,
            tasksList: tasksLists,
            busy: busyTasksSelectorForUserAndDay(state, tasksLists)
        }
    }
    return mapStateToPropsInternal
}



const mapDispatchToProps = (dispatch): ReduxPropsFunc => {
    return {
        onTaskCheckedStateChangedFunc: (caller: TaskUserDTO, challengeId: number, taskProgress: TaskProgressDTO)=> {
            dispatch(markTaskDoneOrUndone(caller, challengeId, taskProgress));
        },
        onEditTask: (task: TaskDTO) => {
            dispatch(OPEN_EDIT_TASK.new(task))
        },
        onShowTaskEvents: (task: TaskDTO, no: number) => {
            dispatch(SHOW_TASK_EVENTS.new({task, no}))
        }

    }
};


export const TaskTable = connect(mapStateToProps as any, mapDispatchToProps)(ResizeAware(TaskTableInternal)) as React.ComponentClass<Props>;

