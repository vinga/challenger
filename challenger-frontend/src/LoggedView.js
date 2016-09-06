import React, {Component} from "react";
import ChallengeActionTable from "./ChallengeActionTable";


var userFirstTable = {no: 0, id:1, authorized: true, userName: "Kami", date: "2016-08-02", actionsList: [
    {
        id: 1,
        icon: "add",
        difficulty: 0,
        actionName: "Learn one feature of React framework",
        actionType: "weekly",
        actionStatus: "Done",
        dueDate: new Date()
    },
    {
        id: 2,
        icon: "swap_calls",
        difficulty: 1,
        actionName: "Read scientific article",
        actionType: "onetime",
        actionStatus: "Pending",
        dueDate: new Date()
    }
]};

var userSecondTable = {no: 1, id:2, authorized: false, userName: "Jack", date: "2016-08-02", actionsList: [
    {id: 3, icon: "fa-book", difficulty: 0, actionName: "Example task 1", actionType: "monthly", actionStatus: "Done"},
    {id: 4, icon: "fa-car", difficulty: 2, actionName: "Fix car", actionType: "daily", actionStatus: "Pending"},
    {
        id: 5,
        icon: "fa-mobile",
        difficulty: 1,
        actionName: "Learn one feature of React framework",
        actionType: "onetime",
        actionStatus: "Pending",
        dueDate: new Date()
    }
]};


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
                                userTableDTO={userFirstTable}
                                no={0}
                                ctx={this.props.ctx}/>
                        </div>
                       {/* <div className="col s2">

                        </div>*/}
                        <div className="col s12 s12 l6">
                            <ChallengeActionTable
                                userTableDTO={userSecondTable}
                                userName={this.props.ctx.hisLabel}
                                no={1}
                                ctx={this.props.ctx}
                            />

                        </div>

                    </div>
                </div>
            </div>);
    }
}