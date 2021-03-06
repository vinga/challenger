import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import Chip from "material-ui/Chip";
import {getColorSuperlightenForUser, getColorLightenForUser} from "../../views/common-components/Colors";
import {TaskStatus, TaskDTO, TaskApprovalDTO, TaskUserDTO} from "../TaskDTO";
import TextInputDialog from "../../views/common-components/TextInputDialog";
import {updateTaskStatus, deleteTask} from "../taskActions";
import {showTaskEvents} from "../../module_events/index";
import {loggedAccountByIdSelector} from "../../module_accounts/accountSelectors";
import {challengeParticipantsSelector, selectedChallengeParticipantsSelector} from "../../module_challenges/challengeSelectors";
import _ = require("lodash");


const styles = {
    wrapper: {
        display: 'flex',
        flexWrap: 'nowrap',
    },
    chip: {
        marginRight: '5px',
        cursor: 'pointer'
    },

};

interface PropsFunc {
    onTaskAccept: (task: TaskDTO)=>void;
    onTaskReject: (task: TaskDTO, taskRejectReason: string)=>void;
    onTaskConversationShow: (task: TaskDTO)=> void;
    onTaskDeleteFunc: (task: TaskDTO)=> void;
}

interface ReduxProps {
    isTaskCreatorLogged?: boolean,
    taskCreatorOrdinal: number,
    userThatCanAcceptIsLogged: boolean
    waitingForAcceptanceLabels: string
    userThatCanAcceptOrdinal: number
    firstLettersOfRejectors?: string
}

interface Props {
    taskDTO: TaskDTO,
    user: TaskUserDTO,
    no: number,


}
interface State {
    showTaskRejectPopup: boolean,
}
const chipWaiting = {
    marginRight: '5px',
    cursor: 'pointer',
    backgroundColor: 'white',
    color: 'red!important'
};
class TaskLabelInternal extends React.Component<ReduxProps & Props & PropsFunc,State> {
    constructor(props) {
        super(props);
        this.state = {
            showTaskRejectPopup: false,
        }
    }

    closedTxt = () => {
        if (this.props.taskDTO.closeDate!=null)
            return " (Closed)";
        else return null;
    }
    render() {
        if (this.props.taskDTO.taskStatus == TaskStatus.accepted) {
            return <div>{this.props.taskDTO.label}{this.closedTxt()}</div>
        } else if (this.props.taskDTO.taskStatus == TaskStatus.rejected) {
            var style = Object.assign({}, styles.chip, {
                backgroundColor: getColorSuperlightenForUser(this.props.taskCreatorOrdinal),
                flexBasis: 'min-content',
                minWidth: '40px'
            });
            return (
                <div style={styles.wrapper}>
                    <div style={{ lineHeight:'15px', display:'flex', flexDirection: 'column', overflow: "hidden",
                                    textOverflow: "ellipsis",minWidth: '50px',flexBasis:'min-content'}}>
                        <div style={{textDecoration: "line-through"}}>{this.props.taskDTO.label}{this.closedTxt()}</div>

                        <div style={{flexDirection: 'row', display:'flex'}}>
                            <div style={{fontSize:'10px', overflow:'hidden',
                                            whiteSpace: "nowrap",
                                            textOverflow: "ellipsis"}}>
                                <b>{this.props.firstLettersOfRejectors}</b>:&nbsp;
                                {_.uniq(this.props.taskDTO.taskApprovals.filter(t=>t.taskStatus == TaskStatus.rejected).map(t=>t.rejectionReason)).join(", ")}
                            </div>
                        </div>
                    </div>
                    {this.props.isTaskCreatorLogged &&
                    <div style={{flexBasis:'fit-content',  display:'flex'}}>
                        <Chip className="clickableChip" style={style}
                              labelStyle={{ fontSize:'12px'}}
                              onTouchTap={()=>{this.props.onTaskDeleteFunc(this.props.taskDTO);}}>
                            <i className="fa fa-trash"></i> Delete
                        </Chip>
                    </div> }
                </div>);
        }


        if (!this.props.userThatCanAcceptIsLogged) {
            return (<div style={styles.wrapper}>
                <div className="taskLabel">{this.props.taskDTO.label}{this.closedTxt()}</div>
                <Chip style={chipWaiting} className="clickableChip">
                    <div style={{lineHeight:'12px',fontSize: '12px',
                    color:getColorLightenForUser(this.props.no)}}>
                        Waiting for {this.props.waitingForAcceptanceLabels} &apos;s<br/> acceptance <i className="fa fa-hourglass-o"></i>
                    </div>
                </Chip>
            </div>);
        } else {
            var chipStyle = Object.assign({}, styles.chip, {backgroundColor: getColorSuperlightenForUser(this.props.userThatCanAcceptOrdinal)});
            return (<div style={styles.wrapper}>
                <div style={{lineHeight:'32px',minWidth:'50px',overflow:'hidden',
                            whiteSpace: "nowrap", textOverflow: "ellipsis", marginRight:'5px'}}>{this.props.taskDTO.label}{this.closedTxt()}</div>
                <div style={{flexBasis:'fit-content',  display:'flex'}}>
                    <Chip className="clickableChip" style={chipStyle}
                          labelStyle={{ fontSize:'12px'}}
                          onTouchTap={ ()=>{ this.props.onTaskAccept(this.props.taskDTO)}}>
                        <i className="fa fa-check"></i> Accept
                    </Chip>

                    <Chip className="clickableChip" style={chipStyle}
                          labelStyle={{ fontSize:'12px'}}
                          onTouchTap={()=>{this.state.showTaskRejectPopup=true;  this.setState(this.state);}}>
                        <i className="fa fa-close"></i> Reject
                    </Chip>

                </div>

                { this.state.showTaskRejectPopup &&
                <TextInputDialog
                    floatingLabelText="Reject reason"
                    closeYes={(str)=>{ this.props.onTaskReject(this.props.taskDTO, str)}}
                    closeDialog={()=>{this.state.showTaskRejectPopup=false; this.setState(this.state);}}
                />
                }

            </div>);
        }
    }


}


const mapStateToProps = (state: ReduxState, ownprops: Props): ReduxProps => {
    var task: TaskDTO = ownprops.taskDTO;
    //var us: Array<UserDTO> = state.challenges.visibleChallenges.filter(ch=>ch.id == state.challenges.selectedChallengeId).pop().userLabels;
    //var taskCreatorOrdinal: Number = us.findIndex(u=>u.id == task.createdByUserId);

    var userThatCanAcceptIsLogged = false;
    var waitingForAcceptanceLabels: string = "";

    if (task.taskApprovals != null && task.taskStatus == TaskStatus.rejected) {
        var firstLettersOfRejectors = task.taskApprovals.filter(ta=>ta.taskStatus == TaskStatus.rejected)
            .map(ta=>challengeParticipantsSelector(state, ta.userId).find(cp=>cp.id == ta.userId)).map(u=>u.label.substring(0, 1).toUpperCase()).join(", ");

    }
    if (task.taskStatus == TaskStatus.waiting_for_acceptance) {


        // if anyof above user is logged, than show Accept Reject
        if (task.taskApprovals != null) {

            var usersWaitingForAcceptance = task.taskApprovals.filter(ta=>ta.taskStatus == TaskStatus.waiting_for_acceptance)
                .map(ta=> challengeParticipantsSelector(state, ta.userId).find(cp=>cp.id == ta.userId)).filter(a=>a != null);
            waitingForAcceptanceLabels = usersWaitingForAcceptance.map(a=>a.label).sort().join(", ");


            // calculate ordinal of first user that can accept
            var userThatCanAcceptOrdinal = -1;
            var participants = selectedChallengeParticipantsSelector(state).some((chp, index)=> {
                if (usersWaitingForAcceptance.find(a=>a.id == chp.id) != null) {
                    userThatCanAcceptOrdinal = index
                    return true;
                } else return false;
            });


            userThatCanAcceptIsLogged = task.taskApprovals.filter(ta=>ta.taskStatus == TaskStatus.waiting_for_acceptance)
                .some(ta=> {
                    return loggedAccountByIdSelector(state, ta.userId) != null
                });
        }


    }

    return {
        isTaskCreatorLogged: ownprops.user.jwtToken != null,
        taskCreatorOrdinal: ownprops.no,
        userThatCanAcceptIsLogged: userThatCanAcceptIsLogged,
        waitingForAcceptanceLabels: waitingForAcceptanceLabels,
        userThatCanAcceptOrdinal: userThatCanAcceptOrdinal,
        firstLettersOfRejectors: firstLettersOfRejectors,
    }
};

const mapDispatchToProps = (dispatch): PropsFunc => {

    return {
        onTaskAccept: (task: TaskDTO)=> {
            var taskApproval: TaskApprovalDTO = {
                userId: -1,
                taskId: task.id,
                taskStatus: TaskStatus.accepted
            };
            dispatch(updateTaskStatus(task.challengeId, taskApproval));
        },
        onTaskReject: (task: TaskDTO, rejectionReason: string) => {
            var taskApproval: TaskApprovalDTO = {
                userId: -1,
                taskId: task.id,
                taskStatus: TaskStatus.rejected,
                rejectionReason: rejectionReason
            };
            dispatch(updateTaskStatus(task.challengeId, taskApproval));
        },
        onTaskConversationShow: (task: TaskDTO) => {
            dispatch(showTaskEvents(task.challengeId, task.id));
        },
        onTaskDeleteFunc: (task: TaskDTO) => {
            dispatch(deleteTask(task));
        }
    }
};

export const TaskLabel = connect(mapStateToProps, mapDispatchToProps)(TaskLabelInternal)

