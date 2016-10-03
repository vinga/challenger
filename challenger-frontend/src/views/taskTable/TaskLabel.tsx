import * as React from "react";
import Chip from "material-ui/Chip";
import colors from "../common-components/Colors.ts";
import {TaskStatus, TaskDTO} from "../../logic/domain/TaskDTO";
import {AccountDTO} from "../../logic/domain/AccountDTO";
import {ReduxState} from "../../redux/ReduxState";
import {connect} from "react-redux";
import TextInputDialog from "../common-components/TextInputDialog";
import {updateTaskStatus} from "../../redux/actions/taskActions";
import {TaskApprovalDTO} from "../../logic/domain/TaskApprovalDTO";

const styles = {
    wrapper: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    chip: {
        marginRight: '5px',
        cursor: 'pointer'
    },

}

interface PropsFunc {
    onTaskAccept:(task:TaskDTO)=>void;
    onTaskReject:(task:TaskDTO, taskRejectReason:string)=>void;
}


interface TaskProps {
    taskDTO:TaskDTO,
    user:AccountDTO,
    no:number,
    isTaskCreatorLogged?:Boolean


}
interface State {
    showTaskRejectPopup:boolean
}

export class TaskLabel extends React.Component<TaskProps & PropsFunc,State> {
    constructor(props) {
        super(props);
        this.state = {
            showTaskRejectPopup: false
        }
    }

    render() {
        if (this.props.taskDTO.taskStatus == TaskStatus.accepted) {
            return <div>{this.props.taskDTO.label}</div>
        } else if (this.props.taskDTO.taskStatus == TaskStatus.rejected) {
            console.log("ISTHIS " + this.props.taskDTO.label + " " + this.props.taskDTO.createdByUserId + " " + this.props.user.userId);
            if (this.props.isTaskCreatorLogged) {//this.props.taskDTO.createdByUserId == this.props.user.userId && this.props.user.jwtToken != null) { //  kto moze dawac do reworku?

                return (
                    <div style={styles.wrapper}>
                        <div className="taskLabel"
                             style={{textDecoration: "line-through"}}>{this.props.taskDTO.label}</div>
                        <Chip style={styles.chip} className="clickableChip"
                              onTouchTap={()=>this.props.onTaskAccept(this.props.taskDTO)}>
                            <i className="fa fa-share"></i> Rework
                        </Chip>

                        <Chip style={styles.chip} className="clickableChip"
                              onTouchTap={()=>{this.state.showTaskRejectPopup=true; this.setState(this.state);}}>
                            <i className="fa fa-trash"></i> Delete
                        </Chip></div>);
            }
            else {
                var chipWaiting = {
                    marginRight: '5px',
                    cursor: 'pointer',
                    backgroundColor: 'white',
                    color: 'red!important'
                }
                return (<div style={styles.wrapper}>
                    <div className="taskLabel" style={{textDecoration: "line-through"}}>{this.props.taskDTO.label}</div>
                    <Chip style={chipWaiting} className="clickableChip">
                        <div style={{lineHeight:'12px',fontSize: '12px',
                    color:colors.userColorsLighten[this.props.no]}}>
                            Waiting for {this.props.taskDTO.createdByUserId}&apos;s<br/> rework or deletion <i
                            className="fa fa-hourglass-o"></i>
                        </div>
                    </Chip>
                </div>);
            }

            //  return <div style={{textDecoration: "line-through"}}>{this.props.taskDTO.label}</div>
        } else if (this.props.user.jwtToken == null && this.props.taskDTO.createdByUserId != this.props.user.userId) {

            var chipWaiting = {
                marginRight: '5px',
                cursor: 'pointer',
                backgroundColor: 'white',
                color: 'red!important'
            }
            return (<div style={styles.wrapper}>
                <div className="taskLabel">{this.props.taskDTO.label}</div>
                <Chip style={chipWaiting} className="clickableChip">
                    <div style={{lineHeight:'12px',fontSize: '12px',
                    color:colors.userColorsLighten[this.props.no]}}>
                        Waiting for {this.props.user.label}&apos;s<br/> acceptance <i className="fa fa-hourglass-o"></i>
                    </div>
                </Chip>
            </div>);


        } else {

            return (<div style={styles.wrapper}>

                <div className="taskLabel">{this.props.taskDTO.label}</div>

                <Chip style={styles.chip} className="clickableChip"
                      onTouchTap={()=>this.props.onTaskAccept(this.props.taskDTO)}>
                    <i className="fa fa-check"></i> Accept
                </Chip>


                <Chip style={styles.chip} className="clickableChip"
                      onTouchTap={()=>{this.state.showTaskRejectPopup=true; this.setState(this.state);}}>
                    <i className="fa fa-close"></i> Reject
                </Chip>

                { this.state.showTaskRejectPopup &&
                <TextInputDialog
                    floatingLabelText="Reject reason"
                    closeYes={(str)=>this.props.onTaskReject(this.props.taskDTO, str)}
                    closeDialog={()=>{this.state.showTaskRejectPopup=false; this.setState(this.state);}}
                />
                }

            </div>);
        }
    }


}


const mapStateToProps = (state:ReduxState, ownprops:any):any => {

    var task: TaskDTO=ownprops.taskDTO;
    var isTaskCreatorLogged:Boolean=state.users.filter(u=>u.userId==task.createdByUserId && u.jwtToken!=null).pop()!=null;

    return {isTaskCreatorLogged: isTaskCreatorLogged}
}

const mapDispatchToProps = (dispatch):any => {

    return {
        onTaskAccept: (task:TaskDTO)=> {
            //dispatch(markTaskDoneOrUndone(challengeId, taskProgress));
            var taskApproval:TaskApprovalDTO = {
                userId: -1,
                taskId: task.id,
                taskStatus: TaskStatus.accepted
            }
            dispatch(updateTaskStatus(taskApproval));
        },
        onTaskReject: (task:TaskDTO, rejectionReason:string) => {
            var taskApproval:TaskApprovalDTO = {
                userId: -1,
                taskId: task.id,
                taskStatus: TaskStatus.rejected,
                rejectionReason: rejectionReason
            }
            dispatch(updateTaskStatus(taskApproval));
        }
    }
}


const Ext = connect(mapStateToProps, mapDispatchToProps)(TaskLabel)

export default Ext;