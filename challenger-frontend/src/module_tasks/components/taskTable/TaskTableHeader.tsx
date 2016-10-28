import * as React from "react";
import {ReduxState, connect} from "../../../redux/ReduxState";
import FlatButton from "material-ui/FlatButton";
import {TaskDTO, TaskStatus, TaskType, TaskUserDTO} from "../../TaskDTO";
import colors from "../../../views/common-components/Colors";
import {OPEN_EDIT_TASK} from "../../taskActionTypes";
import {TaskTableHeaderAccountPanel} from "../../../module_accounts/index";


interface Props {
    tasksList: Array<TaskDTO>,
    user: TaskUserDTO,
    challengeId: number,
    no: number
}

interface ReduxPropsFunc {
    onAddNewTaskFunc: (task: TaskDTO)=>void;

}
interface PropsFunc {
    onOpenDialogForLoginSecondUser: (event: EventTarget)=>void;
}

class TaskTableHeaderInternal extends React.Component<Props & ReduxPropsFunc & PropsFunc,void> {
    constructor(props) {
        super(props);


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
            userId: this.props.user.id,
            challengeId: this.props.challengeId,
            done: false,
            createdByUserId: this.props.user.id,

        };
        return taskDTO;
    }

    render() {

        return (<div>

            <TaskTableHeaderAccountPanel
                onOpenDialogForLoginSecondUser={this.props.onOpenDialogForLoginSecondUser}
                no={this.props.no}
                userId={this.props.user.id}
                userLabel={this.props.user.label}
                userLogin={this.props.user.login}
            />

            <div style={{clear: 'both'}}></div>
            <span className="left" style={{margin: '3px'}}>Points: {this.calculateCheckedCount()}</span>
            <div className="right" style={{display: "inline-block"}}>
                <FlatButton
                    onClick={()=>this.props.onAddNewTaskFunc(this.createNewTask())}
                    label="Add task"
                    labelPosition="before"
                    primary={true}
                    style={{color: colors.userColors[this.props.no]}}
                />
            </div>
        </div>);
    }


}

const mapStateToProps = (state: ReduxState, ownProps: Props & PropsFunc): {} => {
    return {};
};
const mapDispatchToProps = (dispatch): ReduxPropsFunc => {
    return {
        onAddNewTaskFunc: (task: TaskDTO) => {
            dispatch(OPEN_EDIT_TASK.new(task))
        }

    }
};
export const TaskTableHeader = connect(mapStateToProps, mapDispatchToProps)(TaskTableHeaderInternal);



