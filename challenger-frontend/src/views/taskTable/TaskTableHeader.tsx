import * as React from "react";
import TaskTableUserIcon from "./TaskTableUserIcon.tsx";
import FlatButton from "material-ui/FlatButton";
import colors from "../common-components/Colors.ts";
import {TaskDTO, TaskType, TaskStatus} from "../../logic/domain/TaskDTO";
import {OPEN_EDIT_TASK, ON_LOGOUT_SECOND_USER} from "../../redux/actions/actions";
import {connect} from "react-redux";
import {IconButton} from "material-ui";
import {AccountDTO} from "../../logic/domain/AccountDTO";


interface Props {
    tasksList:Array<TaskDTO>,
    user:AccountDTO,
    challengeId:number,
    no:number
}

interface ReduxPropsFunc {
    onAddNewTaskFunc:(task:TaskDTO)=>void;
    onSecondUserLogout:(userId:number)=>void;

}
interface PropsFunc {
    onOpenDialogForLoginSecondUser:(event:EventTarget)=>void;
}

class TaskTableHeader extends React.Component<Props & ReduxPropsFunc & PropsFunc,void> {
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
            userId: this.props.user.userId,
            challengeId: this.props.challengeId,
            done: false,
            createdByUserId: this.props.user.userId,

        };
        return taskDTO;
    }


    // <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>  / <span>{this.calculateAllCount()}</span>
    render() {

        return (<div>


            <h5  >
                <TaskTableUserIcon
                    userNo={this.props.no}
                />

                <span style={{}}><span style={{lineHeight: '65px'}}>{this.props.user.label}</span>
                    {this.props.no != 0 && (this.props.user.jwtToken != null ?

                            <IconButton
                                onClick={() => {this.props.onSecondUserLogout(this.props.user.userId)}}>
                                &nbsp;<i className={'fa fa-power-off' }
                                         style={{marginTop: '3px',fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                            </IconButton>

                            :

                            <IconButton
                                onClick={(event) => this.props.onOpenDialogForLoginSecondUser(event.currentTarget)}>
                                &nbsp;<i className={'fa fa-lock' }
                                         style={{marginTop: '3px',fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                            </IconButton>

                    )}{this.props.user.errorDescription != null &&
                    <span className="red-text text-darken-3"
                          style={{fontSize:'15px'}}>
                        {this.props.user.errorDescription}</span>}
                </span>

            </h5>
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

const mapStateToProps = (state, ownProps:Props & PropsFunc):{} => {
    return {};
};
const mapDispatchToProps = (dispatch):ReduxPropsFunc => {
    return {
        onAddNewTaskFunc: (task:TaskDTO) => {
            dispatch(OPEN_EDIT_TASK.new(task))
        },
        onSecondUserLogout: (userId:number) => {
            dispatch(ON_LOGOUT_SECOND_USER.new({userId}));
        }
    }
};

const Ext = connect(mapStateToProps, mapDispatchToProps)(TaskTableHeader);

export default Ext;


