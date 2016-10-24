import * as React from "react";
import {ReduxState, connect} from "../redux/ReduxState";


import {DisplayedEventUI} from "./EventGroup";
import {EventGroup} from "./EventGroup";

import {TaskDTO, TaskTable, EditTaskDialog} from "../module_tasks/index";
import {AccountDTO, loggedUserSelector} from "../module_accounts/index";
import {sendEvent, selectedChallengeSelector, challengeAccountsSelector, challengeEventsSelector} from "../module_challenges/index";


interface Props {
    challengeIsSelected: boolean,
    editedTask?: TaskDTO,
    userId: number,
    accounts: Array<AccountDTO>,
    displayedPosts?: Array<DisplayedEventUI>
}

interface PropsFunc {
    onSendEventFunc: (content: string) => void
}

class LoggedView extends React.Component<Props & PropsFunc,void> {

    render() {
        var rows = [];

        if (this.props.challengeIsSelected) {
            let i = 0;
            var comps = this.props.accounts.map(u=>
                <div className="col s12 m6">
                    <TaskTable no={i++} user={u}/>
                </div>
            );
            // fit exactly two tables in one row
            for (i = 0; i < comps.length; i += 2) {
                rows.push(<div className="row" key={i}>{comps[i]}{i + 1 < comps.length && comps[i + 1]}</div>)
            }
        }

        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    {rows}
                </div>
                {
                    this.props.editedTask!=null &&
                        <EditTaskDialog task={this.props.editedTask}/>
                }
                {
                    this.props.displayedPosts != null &&
                        <EventGroup
                            displayedEvents={this.props.displayedPosts}
                            onPostEventFunc={this.props.onSendEventFunc}
                        />
                }
            </div>);
    }
}

const mapStateToProps = (state: ReduxState): Props => {
    return {
        editedTask: state.tasksState.editedTask,
        userId: loggedUserSelector(state).userId,
        challengeIsSelected: selectedChallengeSelector(state) != null,
        accounts: challengeAccountsSelector(state),
        displayedPosts: challengeEventsSelector(state)

    }
};


const mapDispatchToProps = (dispatch) => {
    return {
        onSendEventFunc: (content:string) => {
            dispatch(sendEvent(content))
        }
    }
};

let Ext = connect(mapStateToProps, mapDispatchToProps)(LoggedView);
export default Ext;