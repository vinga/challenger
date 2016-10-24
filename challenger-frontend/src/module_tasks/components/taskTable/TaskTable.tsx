import * as React from "react";
import {ReduxState, connect} from "../../../redux/ReduxState";
import {Table, TableBody, TableRow, TableRowColumn} from "material-ui/Table";
import Paper from "material-ui/Paper";
import DifficultyIconButton from "./DifficultyIconButton.tsx";
import ChallengeTableCheckbox from "./ChallengeTableCheckbox.tsx";
import TaskTableHeader from "./TaskTableHeader.tsx";
import {TaskDTO, TaskProgressDTO, createTaskDTOListKey, TaskDTOListForDay} from "../../TaskDTO";
import {WebState} from "../../../logic/domain/Common";
import {markTaskDoneOrUndone} from "../../taskActions";
import {OPEN_EDIT_TASK} from "../../taskActionTypes";
import {ResizeAware} from "../../../views/Constants";
import {TaskLabel} from "../TaskLabel";
import {AccountDTO, SecondUserAuthorizePopover} from "../../../module_accounts/index";
import {selectedChallengeSelector} from "../../../module_challenges/index";


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
    no:number,
    challengeId:number,
    currentDate:Date,
    user:AccountDTO,
    tasksList:Array<TaskDTO>,
    busy:boolean,



}
interface PropsFunc {
    onTaskCheckedStateChangedFunc:(challengeId:number, taskProgress:TaskProgressDTO)=>void;
    onEditTask:(task:TaskDTO)=>void;
}
interface State {
    popoverAnchorEl?: React.ReactInstance;
    authorizePanel:boolean
}

class TaskTableInternal extends React.Component<Props & PropsFunc, State> {
    constructor(props) {
        super(props);
        this.state = {
            authorizePanel: false
        }
    }

    handleResize = (e) => {
        this.setState(this.state);
    };

    onTaskCheckedStateChangedFunc = (taskDTO) => {
        var taskProgressDTO = {
            taskId: taskDTO.id,
            done: taskDTO.done,
            progressTime: this.props.currentDate.getTime()
        };
        this.props.onTaskCheckedStateChangedFunc(this.props.challengeId, taskProgressDTO);
    };



    showAuthorizePanelFuncFromHeader = (anchor) => {
        if (this.props.user.jwtToken==null) {
            this.state.authorizePanel = true;
        }
        if (this.state.authorizePanel) {
            this.state.popoverAnchorEl=anchor;
           /* this.authPopover.setState({
                anchorEl: anchor,
                open: true
            });*/
            this.setState(this.state);
            return true;
        }
        return false;
    };
    showAuthorizePanelFunc = (anchor, isInputChecked) => {
        if (this.props.user.jwtToken==null) {
            this.state.authorizePanel = true;
        }
        if (this.state.authorizePanel) {
            this.state.popoverAnchorEl=anchor;
            this.setState(this.state);
            return true;
        }
        return false;
    };

    render() {
        var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 400) + "px";
        var other = {minHeight: height, height: height, overflowY: "auto", overflowX: "none"};

        //TODO delay wyszarzenie jesli call nie wrocil
        if (this.props.busy) {
            Object.assign(other, {opacity: "0.4"});
        }

        return (
            <div style={{marginRight: '10px', marginLeft: '10px', marginTop: '20px', marginBottom: '20px'}}>
                <TaskTableHeader no={this.props.no}
                                 user={this.props.user}
                                 tasksList={this.props.tasksList }
                                 challengeId={this.props.challengeId}
                                 onOpenDialogForLoginSecondUser={(eventTarget:EventTarget)=>this.showAuthorizePanelFuncFromHeader(eventTarget)}
                />
                <Paper style={{padding: '10px', display: "inline-block"}}>
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
                                                taskDTO={task}
                                                showAuthorizePanelFunc={this.showAuthorizePanelFunc}
                                                onTaskCheckedStateChangedFunc={this.onTaskCheckedStateChangedFunc}
                                                authorized={this.props.user.jwtToken!=null}
                                            />
                                        </TableRowColumn>
                                    </TableRow>
                                )}
                           </TableBody>
                        </Table>
                    </div>
                </Paper>

                <SecondUserAuthorizePopover
                    user={this.props.user}
                    anchorEl={this.state.popoverAnchorEl}
                    handleRequestClose={()=>{this.state.popoverAnchorEl=null; this.setState(this.state);}}/>
            </div>
        );
    }
}


const mapStateToProps = (state:ReduxState, ownprops:any):any => {
    var challengeId:number =selectedChallengeSelector(state).id;
    var key:string = createTaskDTOListKey(challengeId, state.currentSelection.day);
    var currentTaskListDTO:TaskDTOListForDay=state.tasksState.tasks[key];
    var tasksList;
    var busy = false;
    if (currentTaskListDTO != null) {
        tasksList = currentTaskListDTO.taskList.filter((t:TaskDTO)=>t.userId == ownprops.user.userId);
        busy = currentTaskListDTO.webState == WebState.FETCHING_VISIBLE;
        if (busy) {
            busy=tasksList.filter((t:TaskDTO)=>currentTaskListDTO.invalidTasksIds.contains(t.id)).length>0;
        }
    } else tasksList = [];
    return {
        user: ownprops.user,
        challengeId: challengeId,
        currentDate: state.currentSelection.day,
        tasksList: tasksList,
        busy: busy
    }
};

const mapDispatchToProps = (dispatch):PropsFunc => {
    return {
        onTaskCheckedStateChangedFunc: (challengeId:number, taskProgress:TaskProgressDTO)=> {
            dispatch(markTaskDoneOrUndone(challengeId, taskProgress));
        },
        onEditTask: (task:TaskDTO) => {
            dispatch(OPEN_EDIT_TASK.new(task))
        }
    }
};


export const TaskTable = connect(mapStateToProps, mapDispatchToProps)(ResizeAware(TaskTableInternal));

