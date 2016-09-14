import React, {Component} from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import Header from "./views/Header";
import jwtDecode from "jwt-decode";
import LoginPanel from "./views/LoginPanel";
import LoggedView from "./views/LoggedView";
import ajaxWrapper from "./logic/AjaxWrapper";
import store from "./redux/store";
import {Provider} from "react-redux";

ajaxWrapper.baseUrl = "http://localhost:9080/api";


var contextDTO = {
    me: {
        label: "",
        id: 0,
    },
    him: {
        label: "",
        id: 0,
    },

}


class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            ctx: contextDTO,
            logged: false,
            selectedChallenge: null,
            currentDate: new Date()
        };
    }

    onSelectedChallengeChanged = (challengeDTO) => {
        this.state.selectedChallenge = challengeDTO;
        this.state.ctx.me.label = challengeDTO.myId == challengeDTO.firstUserId ? challengeDTO.firstUserLabel : challengeDTO.secondUserLabel;
        this.state.ctx.him.label = challengeDTO.myId != challengeDTO.firstUserId ? challengeDTO.firstUserLabel : challengeDTO.secondUserLabel;
        this.state.ctx.me.id = challengeDTO.myId;
        this.state.ctx.him.id = challengeDTO.myId == challengeDTO.firstUserId ? challengeDTO.secondUserId : challengeDTO.firstUserId;

        this.setState(this.state);
    }

    onLoggedJWT = (login, webToken) => {
        ajaxWrapper.webToken = webToken;
        this.state.ctx.me.id = jwtDecode(webToken).info.userId;
        this.state.logged = true;
        this.setState(this.state);
    }
    onLogout = () => {
        ajaxWrapper.webToken = null;
        this.state.logged = false;
        this.setState(this.state);
    }

    onCurrentDateChange = (date) => {
        this.state.currentDate = date;
        this.setState(this.state)
    }

    render() {
        return (
            <MuiThemeProvider>
                    <div>
                        <Header
                            logged={this.props.logged}
                            onLogout={this.onLogout}
                            ctx={this.state.ctx}
                            onSelectedChallengeChanged={this.onSelectedChallengeChanged}
                            onCurrentDateChangeFunc={this.onCurrentDateChange}
                            currentDate={this.state.currentDate}
                        />


                        { this.props.logged
                            ?
                            <LoggedView ctx={this.state.ctx}
                                        firstUserDTO={this.state.ctx.me}
                                        secondUserDTO={this.state.ctx.him}
                                        selectedChallengeDTO={this.state.selectedChallenge}
                                        currentDate={this.state.currentDate}
                            />
                            :
                            <LoginPanel
                                ctx={this.state.ctx}
                                onLoggedJWT={this.onLoggedJWT}/>
                        }
                    </div>
            </MuiThemeProvider >
        );
    }
}


const mapStateToProps = (state) => {
    var logged=state.users.filter(u=>u.primary==true).map(u=>{
        return u.jwtToken != null;
    }).pop();
    return {
        logged: logged,
    }
}

import { connect } from 'react-redux'
let ConnectedApp = connect(
    mapStateToProps
)(App)

let ProvidedApp = (serverProps) => {
    return (
        <Provider store={store}>
            <ConnectedApp/>
        </Provider>
    )
}
export default ProvidedApp;

//start node server.js
