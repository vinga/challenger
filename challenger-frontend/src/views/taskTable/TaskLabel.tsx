import * as React from "react";
import Chip from "material-ui/Chip";
import colors, {getColorSuperlightenForUser} from "../common-components/Colors.ts";
import {TaskStatus, TaskDTO} from "../../logic/domain/TaskDTO";
import {AccountDTO} from "../../logic/domain/AccountDTO";
import {ReduxState} from "../../redux/ReduxState";
import {connect} from "react-redux";
import TextInputDialog from "../common-components/TextInputDialog";
import {updateTaskStatus, showTaskConversation} from "../../redux/actions/taskActions";
import {TaskApprovalDTO} from "../../logic/domain/TaskApprovalDTO";
import {UserDTO} from "../../logic/domain/UserDTO";
import {ConversationDTO} from "../../logic/domain/ConversationDTO";

const styles = {
    wrapper: {
        display: 'flex',
        flexWrap: 'nowrap',
    },
    chip: {
        marginRight: '5px',
        cursor: 'pointer'
    },

}

interface PropsFunc {
    onTaskAccept:(task:TaskDTO)=>void;
    onTaskReject:(task:TaskDTO, taskRejectReason:string)=>void;
    onTaskConversationShow:(task: TaskDTO)=> void;
}


interface TaskProps {
    taskDTO:TaskDTO,
    user:AccountDTO,
    no:number,
    isTaskCreatorLogged?:Boolean,
    taskCreatorOrdinal:Number


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
            //console.log("ISTHIS " + this.props.taskDTO.label + " " + this.props.taskDTO.createdByUserId + " " + this.props.user.userId);

            if (this.props.isTaskCreatorLogged) {//this.props.taskDTO.createdByUserId == this.props.user.userId && this.props.user.jwtToken != null) { //  kto moze dawac do reworku?
                var style = Object.assign({}, styles.chip, {backgroundColor: getColorSuperlightenForUser(this.props.taskCreatorOrdinal), flexBasis: 'min-content', minWidth: '40px'});
                return (
                    <div style={styles.wrapper}>
                        <div
                            style={{ lineHeight:'15px', display:'flex', flexDirection: 'column', overflow: "hidden",
  textOverflow: "ellipsis",minWidth: '50px',flexBasis:'min-content'}}>
                            <div>{this.props.taskDTO.label}</div>

                            <div style={{flexDirection: 'row', display:'flex'}}>
                                <div style={{fontSize:'10px', overflow:'hidden',
                            whiteSpace: "nowrap", textOverflow: "ellipsis"}}><b>K</b>: {this.props.taskDTO.taskApproval.rejectionReason}
                                    to jest dlugi komentarz limitowany jak dlugoo blah blah blah blah blah blah
                                </div>
                                <a style={{lineHeight:'16px', marginRight:'5px', color:'#444'}} className="fa fa-comment" onClick={()=>this.props.onTaskConversationShow(this.props.taskDTO)}/></div>
                        </div>
                        <div style={{flexBasis:'fit-content',  display:'flex'}}>

                            <Chip className="clickableChip" style={style}
                                  labelStyle={{ fontSize:'12px'}}
                                  onTouchTap={()=>this.props.onTaskAccept(this.props.taskDTO)}>
                                <i className="fa fa-share"></i> Rework
                            </Chip>

                            <Chip className="clickableChip" style={style}
                                  labelStyle={{ fontSize:'12px'}}
                                  onTouchTap={()=>{this.state.showTaskRejectPopup=true; this.setState(this.state);}}>
                                <i className="fa fa-trash"></i> Delete
                            </Chip>
                        </div>
                    </div>);

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
            var chipStyle = Object.assign({}, styles.chip, {backgroundColor: getColorSuperlightenForUser(this.props.no)});

            return (<div style={styles.wrapper}>



                    <div  style={{lineHeight:'32px',minWidth:'50px',overflow:'hidden',
                            whiteSpace: "nowrap", textOverflow: "ellipsis", marginRight:'5px'}}>{this.props.taskDTO.label}</div>





                    <div style={{flexBasis:'fit-content',  display:'flex'}}>

                        <Chip className="clickableChip" style={chipStyle}
                              labelStyle={{ fontSize:'12px'}}
                              onTouchTap={()=>this.props.onTaskAccept(this.props.taskDTO)}>
                            <i className="fa fa-check"></i> Accept
                        </Chip>

                        <Chip className="clickableChip" style={chipStyle}
                              labelStyle={{ fontSize:'12px'}}
                              onTouchTap={()=>{this.state.showTaskRejectPopup=true; this.setState(this.state);}}>
                            <i className="fa fa-close"></i> Reject
                        </Chip>
                    </div>



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

    var task:TaskDTO = ownprops.taskDTO;
    var isTaskCreatorLogged:Boolean = state.users.filter(u=>u.userId == task.createdByUserId && u.jwtToken != null).pop() != null;


    var us:Array<UserDTO> = state.challenges.visibleChallenges.filter(ch=>ch.id == state.challenges.selectedChallengeId).pop().userLabels;
    var taskCreatorOrdinal:Number = us.findIndex(u=>u.id == task.createdByUserId);
    //state.challenges.visibleChallenges.filter(ch=>ch.id==state.challenges.selectedChallengeId).pop();

    return {isTaskCreatorLogged: isTaskCreatorLogged, taskCreatorOrdinal: taskCreatorOrdinal}
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
        },
        onTaskConversationShow: (task: TaskDTO) => {
            dispatch(showTaskConversation(task));
        }
    }
}


const Ext = connect(mapStateToProps, mapDispatchToProps)(TaskLabel)

export default Ext;