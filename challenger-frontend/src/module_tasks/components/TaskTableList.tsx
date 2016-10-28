import * as React from "react";
import {TaskTable} from "./taskTable/TaskTable";
import {TaskUserDTO} from "../TaskDTO";


interface Props {
    challengeId?: number,
    accounts: Array<TaskUserDTO>

    showAuthorizeFuncIfNeeded: (eventTarget: EventTarget, userId: number)=>JQueryPromise<boolean>
}

export class TaskTableList extends React.Component<Props,void> {


    render() {
        var rows = [];
        if (this.props.challengeId != null) {
            let i = 0;
            var comps = this.props.accounts.map(u=>
                <div className="col s12 m6">
                    <TaskTable
                        no={i++}
                        user={u}
                        showAuthorizeFuncIfNeeded={this.props.showAuthorizeFuncIfNeeded}
                        userIsAuthorized={u.jwtToken!=null}
                        challengeId={this.props.challengeId}/>
                </div>
            );
            // fit exactly two tables in one row
            for (i = 0; i < comps.length; i += 2) {
                rows.push(<div className="row" key={i}>{comps[i]}{i + 1 < comps.length && comps[i + 1]}</div>)
            }
        }
        return <div>{rows}</div>;
    }
}