import * as React from "react";
import {ReduxState, connect} from "../redux/ReduxState";
import {TaskTableList} from "../module_tasks/index";
import {loggedUserSelector, SecondUserAuthorizePopover, AccountDTO} from "../module_accounts/index";
import {selectedChallengeIdSelector, challengeAccountsSelector} from "../module_challenges/index";
import {EventGroupPanel} from "../module_events/index";
import {ChallengeDTO} from "../module_challenges/ChallengeDTO";
import {EditChallengeDialog} from "../module_challenges/components/EditChallengeDialog";

interface ReduxProps {
    challengeId?: number,
    userId: number,
    challengeAccounts: Array<AccountDTO>,
    editChallenge: boolean

}


class LoggedView extends React.Component<ReduxProps,void> {
    private secondUserAuthorizePopover: any;


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
                            return this.secondUserAuthorizePopover.getWrappedInstance().showAuthorizeFuncIfNeeded(eventTarget, userId)
                        }}
                    />
                </div>

                {
                    this.props.challengeId != null &&
                    <EventGroupPanel authorId={this.props.userId}/>
                }


                <SecondUserAuthorizePopover
                    ref={ (c) =>this.secondUserAuthorizePopover=c}
                    challengeAccounts={this.props.challengeAccounts}
                />
                {
                    this.props.editChallenge == true &&
                     <EditChallengeDialog/>
                }

            </div>);
    }
}

const mapStateToProps = (state: ReduxState): ReduxProps => {
    return {
        userId: loggedUserSelector(state).userId,
        challengeId: selectedChallengeIdSelector(state),
        challengeAccounts: challengeAccountsSelector(state),
        editChallenge: state.challenges.editedChallenge != null
    }
};


let Ext = connect(mapStateToProps)(LoggedView);
export default Ext;