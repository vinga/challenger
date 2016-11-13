import * as React from "react";
import {ReduxState, connect} from "../redux/ReduxState";
import {TaskTableList} from "../module_tasks/index";
import {loggedUserSelector, SecondUserAuthorizePopover, AccountDTO} from "../module_accounts/index";
import {selectedChallengeIdSelector, challengeAccountsSelector} from "../module_challenges/index";
import {EventGroupPanel} from "../module_events/index";
import {EditChallengeDialog} from "../module_challenges/components/EditChallengeDialog";
import Chart = require("chart.js");


interface ReduxProps {
    challengeId?: number,
    userId: number,
    challengeAccounts: Array<AccountDTO>,
    editChallenge: boolean

}


class LoggedView extends React.Component<ReduxProps,void> {
    private secondUserAuthorizePopover: any;


    componentDidMount = () => {
        console.log("DDD");
        var myChart = new Chart("myChart" as any, {
            type: 'bar',
            data: {
                labels: ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"],
                datasets: [{
                    label: '# of Votes',
                    data: [12, 19, 3, 5, 2, 3],

                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
        var myChart2 = new Chart("myChart2" as any, {
            type: 'bar',
            data: {
                labels: ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"],
                datasets: [{
                    label: '# of Votes',
                    data: [12, 19, 3, 5, 2, 3],

                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
    }

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


                <div className="row">
                    <div className="col m6">
                        <canvas id="myChart" width="200" height="50px"></canvas>
                    </div>
                    <div className="col m6">
                        <canvas id="myChart2" width="200" height="50px"></canvas>
                    </div>
                </div>

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