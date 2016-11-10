import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {TaskTable} from "./taskTable/TaskTable";
import {TaskUserDTO, TaskDTO} from "../TaskDTO";
import {EditTaskDialog} from "./taskEditWindow/EditTaskDialog";


interface Props {
    challengeId?: number,
    accounts: Array<TaskUserDTO>,
    showAuthorizeFuncIfNeeded: (eventTarget: EventTarget, userId: number)=>JQueryPromise<boolean>
}
interface ReduxProps {
    editedTask?: TaskDTO,
}
class TaskTableListInternal extends React.Component<Props & ReduxProps, void> {


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
        return <div>{rows}

            {
                this.props.editedTask != null &&
                <EditTaskDialog task={this.props.editedTask}/>
            }
        </div>;
    }
}

const mapStateToProps = (state: ReduxState, ownProps: Props): ReduxProps => {
    return {
        editedTask: state.tasksState.editedTask,
    }
};


export const TaskTableList = connect(mapStateToProps)(TaskTableListInternal);
