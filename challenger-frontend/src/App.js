import React, {Component} from "react";
import ChallengeActionTable from "./ChallengeActionTable";
import htmlHeader from "./html/header.html";
import htmlFooter from "./html/footer.html";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import LoginPanel from "./LoginPanel";
import Header from "./Header";


var userFirstList = [
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
];
var userFirstTable = {no: 0, authorized: true, userName: "Kami", date: "2016-08-02", actionsList: userFirstList};


var userSecondList = [
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
];
var userSecondTable = {no: 1, authorized: false, userName: "Jack", date: "2016-08-02", actionsList: userSecondList};


var contextDTO = {
    baseUrl: "http://localhost:9080/api",
    userColors: ["#ffcc80", "#80deea"],
    userColorsDarken3: ["#ef6c00", "#00838f"],
    userColorsTextClass: ["orange-text", "cyan-text"],
    logged: false,
    user1: {
        userName: "unknown"
    }
}

class LoggedView extends React.Component {
    render() {
        return (
            <div id="main" className="container" style={{minHeight: '300px'}}>
                <div className="section">
                    <div className="row">
                        <div className="col s12 m5">
                            <ChallengeActionTable userTableDTO={userFirstTable}
                                                  ctx={this.props.ctx}/>
                        </div>
                        <div className="col s2">

                        </div>
                        <div className="col s12 m5">
                            <ChallengeActionTable userTableDTO={userSecondTable}
                                                  ctx={this.props.ctx}
                            />

                        </div>

                    </div>
                </div>
            </div>);
    }
}

export default class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            ctx: contextDTO
        };

    }


    onLoggedJWT = (login, webToken) => {
        this.state.ctx.webToken=webToken;
        this.state.ctx.logged=true;
        this.state.ctx.user1.userName=login;
        this.setState({ctx: this.state.ctx});
    }
    onLogout = () => {
        this.state.ctx.logged=false;
        this.setState({ctx: this.state.ctx});
    }


    render() {


        return (
            <MuiThemeProvider>
                <div>

                    <div>
                        <Header
                            userName={this.state.ctx.user1.userName}
                            logged={this.state.ctx.logged}
                            onLogout={this.onLogout}
                        />


                        { (this.state.ctx.logged === true)
                                ?
                                <LoggedView ctx={this.state.ctx}/>
                                :
                                <LoginPanel
                                    ctx={this.state.ctx}
                                    onLoggedJWT={this.onLoggedJWT}/>
                        }

                    </div>
                    <div dangerouslySetInnerHTML={ {__html: htmlFooter} }/>
                </div>
            </MuiThemeProvider >
        );
    }
}

//start node server.js
