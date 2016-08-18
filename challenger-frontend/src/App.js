import React, {Component} from "react";
import ChallengeActionTable from "./ChallengerComponents";
import htmlHeader from "./html/header.html";
import htmlFooter from "./html/footer.html";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import RaisedButton from "material-ui/RaisedButton";
import injectTapEventPlugin from 'react-tap-event-plugin';

// Needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
injectTapEventPlugin();

var userFirstList = [
    {
        id: 1,
        icon: "add",
        difficulty: 0,
        actionName: "Learn one feature of React framework",
        actionType: "Weekly",
        actionStatus: "Done"
    },
    {id: 2, icon: "swap_calls",  difficulty: 1, actionName: "Read scientific article", actionType: "Adhoc", actionStatus: "Pending"}
];
var userFirstTable = {no: 0, authorized: true, userName: "Kami", date: "2016-08-02", actionsList: userFirstList};


var userSecondList = [
    {id: 3, icon: "fa-book",  difficulty: 0, actionName: "Example task 1", actionType: "Monthly", actionStatus: "Done"},
    {id: 4, icon: "fa-car",  difficulty: 2, actionName: "Fix car", actionType: "Daily", actionStatus: "Pending"},
    {
        id: 5,
        icon: "fa-mobile",
        difficulty: 1,
        actionName: "Learn one feature of React framework",
        actionType: "Adhoc",
        actionStatus: "Pending"
    }
];
var userSecondTable = {no: 1, authorized: false, userName: "Jack", date: "2016-08-02", actionsList: userSecondList};



export default class App extends Component {
    constructor(props) {
        super(props);

    }

// <RaisedButton label="Default"/>
    render() {

        return (
            <MuiThemeProvider>
                <div>

                    <div>
                        <div dangerouslySetInnerHTML={ {__html: htmlHeader} }/>
                        <div id="main" className="container">
                            <div className="section">
                                <div className="row">
                                    <div className="col s12 m5">

                                        <ChallengeActionTable userTableDTO={userFirstTable}
                                                              url="http://localhost:9080/api/challengeActions"/>
                                    </div>
                                    <div className="col s2">

                                    </div>
                                    <div className="col s12 m5">
                                        <ChallengeActionTable userTableDTO={userSecondTable}
                                                              url="http://localhost:9080/api/challengeActions"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div dangerouslySetInnerHTML={ {__html: htmlFooter} }/>
                    </div>
                </div>
            </MuiThemeProvider>


        );
    }
}

//start node server.js
