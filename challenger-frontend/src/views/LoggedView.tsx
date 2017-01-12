import * as React from "react";
import {ReduxState, connect} from "../redux/ReduxState";
import {UserSlot} from "./UserSlot";
import {loggedUserSelector, SecondUserAuthorizePopover} from "../module_accounts/index";
import {ChallengeParticipantDTO, EditChallengeDialog, selectedChallengeIdSelector, challengeParticipantsSelector} from "../module_challenges/index";
import {EventGroupPanel} from "../module_events/index";
import {TaskDTO, EditTaskDialog} from "../module_tasks/index";
import {Col, Row} from "./common-components/Flexboxgrid";
import NoChallengesPanel from "../module_challenges/components/NoChallengesPanel";
import {challengeStatusSelector} from "../module_challenges/challengeSelectors";
import {ChallengeStatus} from "../module_challenges/ChallengeDTO";
import {GlobalNotificationsPanel} from "../module_events/components/GlobalNotificationsPanel";


interface ReduxProps {
    challengeId?: number,
    challengeIsActive: boolean,
    userId: number,
    challengeAccounts: Array<ChallengeParticipantDTO>,
    editChallenge: boolean,
    editedTask?: TaskDTO,
    globalEventsVisible: boolean

}


class LoggedView extends React.Component<ReduxProps,void> {
    private secondUserAuthorizePopover: any;


    showAuthorizeFuncIfNeeded = (eventTarget: EventTarget, userId: number): Promise<boolean> => {
        return this.secondUserAuthorizePopover.getWrappedInstance().showAuthorizeFuncIfNeeded(eventTarget, userId)
    }

    render() {


        var rows = [];
        if (this.props.challengeIsActive) {
            var comps = this.props.challengeAccounts.map((u, iter)=>
                <Col col="12-5">
                    <UserSlot user={u}
                              challengeId={this.props.challengeId}
                              ordinal={iter}
                              showAuthorizeFuncIfNeeded={this.showAuthorizeFuncIfNeeded}
                    />
                </Col>
            );

            // fit exactly two tables in one row
            for (var i = 0; i < comps.length; i += 2) {
                rows.push(<Row horizontal="center" key={i}>{comps[i]}{i + 1 < comps.length && comps[i + 1]}</Row>)
            }
        }


        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                Odswiezanie info o niepotwierdzonych challengach.<br/>
                Jak sie na onetime zmienia, to close date sie niauaktualnia a powinno<br/>
                Konfirmacja challenga - accept/reject bez potrzeby logowania<br/>
                Przetestować jeśli ktoś jest rejestruje normalkniue a był zaproszony - wtedy konfirmacja maila jest wymagana
                <NoChallengesPanel/>
                <div>{rows}</div>


                {
                    this.props.challengeIsActive &&
                    <EventGroupPanel authorId={this.props.userId}/>
                }


                <SecondUserAuthorizePopover
                    ref={ (c) =>this.secondUserAuthorizePopover=c}
                    challengeAccounts={this.props.challengeAccounts.map(
                        e=>{
                            return {
                                id: e.id,
                                login: e.login,
                                label: e.label,
                                jwtToken: e.jwtToken,
                                inProgress: null,
                                primary: false
                            }
                        }
                    )}
                />
                {this.props.globalEventsVisible &&
                    <GlobalNotificationsPanel/>
                }
                {
                    this.props.editChallenge == true &&
                    <EditChallengeDialog/>
                }
                {
                    this.props.editedTask != null &&
                    <EditTaskDialog task={this.props.editedTask}/>
                }

            </div>);
    }
}

const mapStateToProps = (state: ReduxState): ReduxProps => {
    return {
        userId: loggedUserSelector(state).id,
        challengeId: selectedChallengeIdSelector(state),
        challengeIsActive: challengeStatusSelector(state) == ChallengeStatus.ACTIVE,
        challengeAccounts: challengeParticipantsSelector(state),
        editChallenge: state.challenges.editedChallenge != null,
        editedTask: state.tasksState.editedTask,
        globalEventsVisible: state.eventsState.globalEventsVisible
    }
};


let Ext = connect(mapStateToProps)(LoggedView);
export default Ext;