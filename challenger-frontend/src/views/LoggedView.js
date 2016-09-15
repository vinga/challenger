import React, {Component} from "react";
import TaskTable from "./taskTable/TaskTable";
import {connect} from "react-redux";


class LoggedView extends React.Component {
    render() {
        //console.log("render logged view "+this.props.ctx.selectedChallenge);
        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    <div className="row">
                        <div className="col s12 s12 l6">
                            {this.props.challengeSelected &&
                            <TaskTable no={0}/>
                            }
                        </div>
                        {/* <div className="col s2">

                         </div>*/}
                        <div className="col s12 s12 l6">
                            {this.props.challengeSelected &&
                            <TaskTable no={1}/>}

                        </div>
                    </div>
                </div>
            </div>);
    }
}

const mapStateToProps = (state) => {
   return {
        challengeSelected: state.visibleChallengesDTO.selectedChallengeId!=-1,
    }
}

let Ext = connect(mapStateToProps)(LoggedView)
export default Ext;