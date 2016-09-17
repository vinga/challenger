import * as React from "react";
import TaskTableUserIcon from "./TaskTableUserIcon.tsx";
import FlatButton from "material-ui/FlatButton";
import ChallengeEditDialogWindow from "../taskEditWindow/ChallengeEditDialogWindow.tsx";
import colors from "../common-components/Colors.ts";
import {TaskDTO, TaskType, TaskStatus} from "../../logic/domain/TaskDTO";



interface Props {
    tasksList: Array<TaskDTO>,
    userId: number,
    challengeId: number,
    userLabel:string,
    no: number,
    onTaskSuccessfullyUpdatedFunc: (task: TaskDTO) => void,
}
export default class TaskTableHeader extends React.Component<Props,{showNewWindow:boolean}> {
    constructor(props) {
        super(props);
        this.state = {
            showNewWindow: false
        };

    }

    calculateCheckedCount() {
        return this.props.tasksList.filter(t=>t.taskStatus == TaskStatus.accepted && t.done)
            .map(t=>t.difficulty + 1)
            .reduce((total, num)=>total + num, 0);
    }

    calculateAllCount() {
        this.props.tasksList
            .map(t=>t.difficulty + 1)
            .reduce((total, num)=>total + num, 0);
    }

    onAddNewTaskFunc = () => {
        this.setState({showNewWindow: true});
        // this.refs.challengeEditDialogWindow.handleOpen();
    }
    handleEditWindowCloseFunc = () => {
        this.setState({showNewWindow: false});
    }


    createNewTask() {
        var today = new Date();
        var taskDTO = {
            id: 0,
            icon: "fa-book",
            difficulty: 0,
            label: "Example task 1",
            taskType: TaskType.onetime,
            taskStatus: TaskStatus.waiting_for_acceptance,
            dueDate: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7).getTime(),
            userId: this.props.userId,
            challengeId: this.props.challengeId,
            done: false

        }
        return taskDTO;
    }


    // <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>  / <span>{this.calculateAllCount()}</span>
    render() {

        return (<div>
            <h5 >
                <TaskTableUserIcon
                    userNo={this.props.no}
                />

                <span className="left" style={{margin: '3px', lineHeight: '65px'}}>{this.props.userLabel}</span>

            </h5>
            <div style={{clear: 'both'}}></div>
            <span className="left" style={{margin: '3px'}}>Points: {this.calculateCheckedCount()}</span>
            <div className="right" style={{display: "inline-block"}}>
                <FlatButton
                    onClick={this.onAddNewTaskFunc}
                    label="Add task"
                    labelPosition="before"
                    primary={true}
                    style={{color: colors.userColors[this.props.no]}}
                />
            </div>

            {this.state.showNewWindow &&
            <ChallengeEditDialogWindow
                open={this.state.showNewWindow}
                onCloseFunc={this.handleEditWindowCloseFunc}
                onTaskSuccessfullyUpdatedFunc={this.props.onTaskSuccessfullyUpdatedFunc}
                taskDTO={this.createNewTask()}

            />
            }
        </div>);
    }


}


