import React, {Component} from "react";
import ChallengeActionTable from "./views/ChallengeActionTable";



export default class LoggedView extends React.Component {
    render() {
        //console.log("render logged view "+this.props.ctx.selectedContract);
        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    <div className="row">
                        <div className="col s12 s12 l6">
                            <ChallengeActionTable
                                userName={this.props.ctx.myLabel}
                                selectedContract={this.props.selectedContract}
                                no={0}
                                ctx={this.props.ctx}/>
                        </div>
                       {/* <div className="col s2">

                        </div>*/}
                        <div className="col s12 s12 l6">
                            <ChallengeActionTable

                                userName={this.props.ctx.hisLabel}
                                no={1}
                                selectedContract={this.props.selectedContract}

                                ctx={this.props.ctx}
                            />

                        </div>

                    </div>
                </div>
            </div>);
    }
}