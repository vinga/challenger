import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import Chip from "material-ui/Chip";
import colors, {getColorSuperlightenForUser} from "../../views/common-components/Colors.ts";
import {TaskStatus, TaskDTO, TaskApprovalDTO, TaskUserDTO} from "../TaskDTO";
import TextInputDialog from "../../views/common-components/TextInputDialog";
import {updateTaskStatus} from "../taskActions";
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
}

interface ReduxProps {
    isTaskCreatorLogged?: boolean,
    taskCreatorOrdinal: number,
    userThatCanAcceptIsLogged: boolean
    waitingForAcceptanceLabels: string
    userThatCanAcceptOrdinal : number
}

interface Props {
    taskDTO: TaskDTO,
    user: TaskUserDTO,
    no: number,


}
interface State {
    showTaskRejectPopup: boolean
}

class TaskLabelInternal extends React.Component<ReduxProps & Props & PropsFunc,State> {
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
            if (this.props.isTaskCreatorLogged) {
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
                };
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
        }



        if (!this.props.userThatCanAcceptIsLogged) {

            var chipWaiting = {
                marginRight: '5px',
                cursor: 'pointer',
                backgroundColor: 'white',
                color: 'red!important'
            };
            return (<div style={styles.wrapper}>
                <div className="taskLabel">{this.props.taskDTO.label}</div>
                <Chip style={chipWaiting} className="clickableChip">
                    <div style={{lineHeight:'12px',fontSize: '12px',
                    color:colors.userColorsLighten[this.props.no]}}>
                        Waiting for {this.props.waitingForAcceptanceLabels} &apos;s<br/> acceptance <i className="fa fa-hourglass-o"></i>
                    </div>
                </Chip>
            </div>);


        } else {
            var chipStyle = Object.assign({}, styles.chip, {backgroundColor: getColorSuperlightenForUser(this.props.userThatCanAcceptOrdinal)});

            return (<div style={styles.wrapper}>


                <div style={{lineHeight:'32px',minWidth:'50px',overflow:'hidden',
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


const mapStateToProps = (state: ReduxState, ownprops: Props): ReduxProps => {
    var task: TaskDTO = ownprops.taskDTO;
    //var us: Array<UserDTO> = state.challenges.visibleChallenges.filter(ch=>ch.id == state.challenges.selectedChallengeId).pop().userLabels;
    //var taskCreatorOrdinal: Number = us.findIndex(u=>u.id == task.createdByUserId);

    var userThatCanAcceptIsLogged=false;
    var waitingForAcceptanceLabels:string="";
    if (task.taskStatus==TaskStatus.waiting_for_acceptance) {


        // if anyof above user is logged, than show Accept Reject
        if (task.taskApprovals!=null) {

            var usersWaitingForAcceptance=task.taskApprovals.filter(ta=>ta.taskStatus==TaskStatus.waiting_for_acceptance)
                .map(ta=>   challengeParticipantsSelector(state,ta.userId).find(cp=>cp.id==ta.userId)).filter(a=>a!=null);



            waitingForAcceptanceLabels=usersWaitingForAcceptance.map(a=>a.label).join();


            // calculate ordinal of first user that can accept
            var userThatCanAcceptOrdinal=-1;
            var participants=selectedChallengeParticipantsSelector(state).some((chp, index)=> {
               if (usersWaitingForAcceptance.find(a=>a.id==chp.id)!=null) {
                   userThatCanAcceptOrdinal=index
                   return true;
               } else return false;
            });



            userThatCanAcceptIsLogged=task.taskApprovals.filter(ta=>ta.taskStatus==TaskStatus.waiting_for_acceptance)
                .some(ta=>  {
                 return loggedAccountByIdSelector(state,ta.userId)!=null });
        }


    }

    return {
        isTaskCreatorLogged: ownprops.user.jwtToken != null,
        taskCreatorOrdinal: ownprops.no,
        userThatCanAcceptIsLogged: userThatCanAcceptIsLogged,
        waitingForAcceptanceLabels: waitingForAcceptanceLabels,
        userThatCanAcceptOrdinal: userThatCanAcceptOrdinal
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
        }
    }
};

export const TaskLabel = connect(mapStateToProps, mapDispatchToProps)(TaskLabelInternal)

