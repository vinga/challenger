import React, {Component} from "react";
import TaskTable from "./taskTable/TaskTable";



class LoggedView extends React.Component {
    render() {
        //console.log("render logged view "+this.props.ctx.selectedChallenge);
        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    <div className="row">
                        <div className="col s12 s12 l6">
                            {this.props.selectedChallengeDTO &&
                            <TaskTable
                                userDTO={this.props.firstUserDTO}
                                selectedChallengeDTO={this.props.selectedChallengeDTO}
                                no={0}
                                currentDate={this.props.currentDate}
                                ctx={this.props.ctx}/>
                            }
                        </div>
                       {/* <div className="col s2">

                        </div>*/}
                        <div className="col s12 s12 l6">
                            {this.props.selectedChallengeDTO &&
                            <TaskTable
                                userDTO={this.props.secondUserDTO}
                                no={1}
                                selectedChallengeDTO={this.props.selectedChallengeDTO}
                                currentDate={this.props.currentDate}
                                ctx={this.props.ctx}
                            />}

                        </div>

                    </div>
                </div>
            </div>);
    }
}

const mapStateToProps = (state) => {
    console.log(state.visibleChallengesDTO.visibleChallenges.filter(ch=>ch.id==state.visibleChallengesDTO.selectedChallengeId).pop());
    return {
        selectedChallengeDTO: state.visibleChallengesDTO.visibleChallenges.filter(ch=>ch.id==state.visibleChallengesDTO.selectedChallengeId).pop(),
        currentDate: state.mainReducer.day
    }
}

import { connect } from 'react-redux'
let Ext = connect(mapStateToProps)(LoggedView)
export default Ext;