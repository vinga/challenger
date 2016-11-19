import * as React from "react";
import {AccountDTO} from "../module_accounts/index";
import {ChartPanel} from "../module_reports/index";
import {TaskTableHeader, TaskTable} from "../module_tasks/index";


interface Props {
    challengeId: number,
    ordinal: number,
    user: AccountDTO,
    showAuthorizeFuncIfNeeded: (eventTarget: EventTarget, userId: number)=>JQueryPromise<boolean>

}

export class UserSlot extends React.Component<Props, void> {


    render() {
        return    <div style={{marginRight: '10px', marginLeft: '10px', marginTop: '20px', marginBottom: '20px'}}>



            <TaskTableHeader no={this.props.ordinal}
                             user={{ id: this.props.user.id,
                            label: this.props.user.label,
                            login: this.props.user.login,
                            jwtToken: this.props.user.jwtToken}}
                             challengeId={this.props.challengeId}
                             onOpenDialogForLoginSecondUser=
                                 {(eventTarget:EventTarget)=>this.props.showAuthorizeFuncIfNeeded(eventTarget, this.props.user.id)}
            >

                <ChartPanel user={{
                            id: this.props.user.id,
                            label: this.props.user.label,
                            ordinal: this.props.ordinal}}
                            challengeId={this.props.challengeId}/>
            </TaskTableHeader>
            <TaskTable
                no={this.props.ordinal}
                user={{ id: this.props.user.id,
                            label: this.props.user.label,
                            login: this.props.user.login,
                            jwtToken: this.props.user.jwtToken}}
                showAuthorizeFuncIfNeeded={this.props.showAuthorizeFuncIfNeeded}
                userIsAuthorized={this.props.user.jwtToken!=null}
                challengeId={this.props.challengeId}/>

        </div>;
    }
}



