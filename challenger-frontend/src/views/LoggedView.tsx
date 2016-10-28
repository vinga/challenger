import * as React from "react";
import {ReduxState, connect} from "../redux/ReduxState";
import {TaskDTO, EditTaskDialog, TaskTableList} from "../module_tasks/index";
import {loggedUserSelector, SecondUserAuthorizePopover, AccountDTO} from "../module_accounts/index";
import {selectedChallengeIdSelector, challengeAccountsSelector} from "../module_challenges/index";
import {EventGroupPanel} from "../module_events/index";

interface ReduxProps {
    challengeId?: number,
    editedTask?: TaskDTO,
    userId: number,

    challengeAccounts: Array<AccountDTO>

}


class LoggedView extends React.Component<ReduxProps,void> {
    private cc: any;


    render() {
        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    <TaskTableList
                        accounts={this.props.challengeAccounts.map(a=> {

                          return {
                            id: a.userId,
                            label: a.label,
                            login: a.login,
                            jwtToken: a.jwtToken
                           }

                        })}
                        challengeId={this.props.challengeId}
                        showAuthorizeFuncIfNeeded={
                        (eventTarget: EventTarget, userId: number) => {
                            return this.cc.getWrappedInstance().showAuthorizeFuncIfNeeded(eventTarget, userId)
                        }}
                    />
                </div>
                {
                    this.props.editedTask != null &&
                    <EditTaskDialog task={this.props.editedTask}/>
                }
                {
                    this.props.challengeId != null &&
                    <EventGroupPanel authorId={this.props.userId}/>
                }


                <SecondUserAuthorizePopover
                    ref={ (c) =>this.cc=c}
                    challengeAccounts={this.props.challengeAccounts}
                />
            </div>);
    }
}

const mapStateToProps = (state: ReduxState): ReduxProps => {
    return {
        editedTask: state.tasksState.editedTask,
        userId: loggedUserSelector(state).userId,
        challengeId: selectedChallengeIdSelector(state),
        challengeAccounts: challengeAccountsSelector(state)
    }
};


let Ext = connect(mapStateToProps)(LoggedView);
export default Ext;