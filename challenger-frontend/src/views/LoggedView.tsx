import * as React from "react";
import TaskTable from "./taskTable/TaskTable.tsx";
import {connect} from "react-redux";
import {ReduxState} from "../redux/ReduxState";
import ChallengeEditDialogWindow from "./taskEditWindow/ChallengeEditDialogWindow.tsx";
import TaskConversation from "./conversation/TaskConversation.tsx";
import {UserDTO} from "../logic/domain/UserDTO";
import {AccountDTO} from "../logic/domain/AccountDTO";
import {ConversationDTO} from "../logic/domain/ConversationDTO";

interface Props {
    challengeSelected:boolean,
    taskIsEdited:boolean,
    userId:number,
    accounts:Array<AccountDTO>,
    displayedConversation?: ConversationDTO;
}

class LoggedView extends React.Component<Props,void> {


    render() {


        var comps = [];    var rows=[];

        if (this.props.challengeSelected) {
            let i = 0;

            this.props.accounts.map(u=> {

                comps.push(<div className="col s12 m6">
                    <TaskTable no={i++} user={u}/>
                </div>);
            })

            for (i = 0; i < comps.length; i += 2) {
                rows.push(<div className="row" key={i}>{comps[i]}{i + 1 < comps.length && comps[i + 1]}</div>)
            }
        }

        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">


                    {rows}


                </div>
                { this.props.taskIsEdited &&
                <ChallengeEditDialogWindow/> }
                {
                    this.props.displayedConversation != null &&
                        <TaskConversation conversation={this.props.displayedConversation}/>
                }


            </div>);
    }
}

const mapStateToProps = (state:ReduxState):Props => {

    var us:Array<UserDTO>=[];
    state.challenges.visibleChallenges.filter(ch=>ch.id == state.challenges.selectedChallengeId).map(c=>us=c.userLabels);
    var accounts=us.map(us=> {
        var account:AccountDTO=state.users.filter(u=>u.userId==us.id).pop();
        if (account!=null) {
            return Object.assign({}, account, us);
        } else {
            return Object.assign({}, us , {userId: us.id} as AccountDTO );
        }
    });

    return {
        challengeSelected: state.challenges.selectedChallengeId != -1,
        taskIsEdited: state.currentSelection.editedTask != null,
        userId: state.currentSelection.userId,
        accounts: accounts,
        displayedConversation: state.currentSelection.displayedConversation
    }
}

let Ext = connect(mapStateToProps)(LoggedView)
export default Ext;