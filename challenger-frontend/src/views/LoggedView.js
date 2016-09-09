import React, {Component} from "react";
import TaskTable from "./taskTable/TaskTable";



export default class LoggedView extends React.Component {
    render() {
        //console.log("render logged view "+this.props.ctx.selectedChallenge);
        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    <div className="row">
                        <div className="col s12 s12 l6">
                            {this.props.selectedChallengeDTO &&
                            <TaskTable
                                userDTO={this.props.ctx.me}
                                selectedChallengeDTO={this.props.selectedChallengeDTO}
                                no={0}
                                ctx={this.props.ctx}/>
                            }
                        </div>
                       {/* <div className="col s2">

                        </div>*/}
                        <div className="col s12 s12 l6">
                            {this.props.selectedChallengeDTO &&
                            <TaskTable
                                userDTO={this.props.ctx.him}
                                no={1}
                                selectedChallengeDTO={this.props.selectedChallengeDTO}
                                ctx={this.props.ctx}
                            />}

                        </div>

                    </div>
                </div>
            </div>);
    }
}