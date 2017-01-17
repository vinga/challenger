import * as React from "react";
import {ChartPanel} from "../module_reports/index";
import {TaskTableHeader, TaskTable} from "../module_tasks/index";
import {ChallengeParticipantDTO} from "../module_challenges/ChallengeDTO";

interface Props {
    challengeId: number,
    ordinal: number,
    user: ChallengeParticipantDTO,
    showAuthorizeFuncIfNeeded: (eventTarget: EventTarget, userId: number) => Promise<boolean>

}


export class UserSlot extends React.Component<Props, void> {


    onOpenDialogForLoginSecondUser = (eventTarget: EventTarget) => {
        this.props.showAuthorizeFuncIfNeeded(eventTarget, this.props.user.id)
    }

    render() {

        return <div style={{marginRight: '10px', marginLeft: '10px', marginTop: '20px', marginBottom: '20px'}}>

            <TaskTableHeader no={this.props.ordinal}
                             user={this.props.user}
                             challengeId={this.props.challengeId}
                             onOpenDialogForLoginSecondUser={this.onOpenDialogForLoginSecondUser}>
                <ChartPanel user={this.props.user}
                            challengeId={this.props.challengeId}/>
            </TaskTableHeader>
            <TaskTable
                no={this.props.ordinal}
                user={this.props.user}
                showAuthorizeFuncIfNeeded={this.props.showAuthorizeFuncIfNeeded}
                userIsAuthorized={this.props.user.jwtToken!=null}
                challengeId={this.props.challengeId}/>

        </div>;
    }
}



