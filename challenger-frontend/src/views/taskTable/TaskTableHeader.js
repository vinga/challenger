import React, {Component} from "react";
import TaskTableUserIcon from "./TaskTableUserIcon";
import FlatButton from "material-ui/FlatButton";
import ChallengeEditDialogWindow from "../ChallengeEditDialogWindow";
import {TaskStatus} from "../Constants";
import colors from "../../logic/Colors";
import {TaskType} from "../Constants";

export default class TaskTableHeader extends React.Component {
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
            icon: "fa-book",
            difficulty: 0,
            label: "Example task 1",
            taskType: TaskType.onetime,
            taskStatus: TaskStatus.pending,
            dueDate: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7).getTime(),
            userId: this.props.userId,
            challengeId: this.props.challengeId
        }
        return taskDTO;
    }
    // <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>  / <span>{this.calculateAllCount()}</span>
    render() {

        return (<div>
            <h5 >
                <TaskTableUserIcon
                    userNo={this.props.no}
                    counts={this.calculateCheckedCount()}
                    userName={this.props.userName}
                />
            </h5>

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

TaskTableHeader.propTypes = {
    tasksList: React.PropTypes.array.isRequired,
    no: React.PropTypes.number.isRequired,
    userDTO: React.PropTypes.object.isRequired,
    challengeId: React.PropTypes.number.isRequired,
    onTaskSuccessfullyUpdatedFunc: React.PropTypes.func.isRequired

};

