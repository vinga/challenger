import React, {Component} from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import Header from "./views/Header";
import jwtDecode from "jwt-decode";
import LoginPanel from "./views/LoginPanel";
import LoggedView from "./views/LoggedView";
import ajaxWrapper from "./logic/AjaxWrapper"

ajaxWrapper.baseUrl= "http://localhost:9080/api";

var contextDTO = {
    me: {
      label: "",
      id:0,
    },
    him: {
        label: "",
        id:0,
    },

}


export default class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            ctx: contextDTO,
            logged: false,
            selectedChallenge: null
        };
    }

       onSelectedChallengeChanged = (challengeDTO) => {
        this.state.selectedChallenge=challengeDTO;
        this.state.ctx.me.label=challengeDTO.myId==challengeDTO.firstUserId ? challengeDTO.firstUserLabel: challengeDTO.secondUserLabel;
        this.state.ctx.him.label=challengeDTO.myId!=challengeDTO.firstUserId ? challengeDTO.firstUserLabel: challengeDTO.secondUserLabel;
        this.state.ctx.me.id=challengeDTO.myId;
        this.state.ctx.him.id=challengeDTO.myId==challengeDTO.firstUserId? challengeDTO.secondUserId: challengeDTO.firstUserId;

        this.setState(this.state);
    }

    onLoggedJWT = (login, webToken) => {
        ajaxWrapper.webToken=webToken;
        this.state.ctx.me.id=jwtDecode(webToken).info.userId;
        this.state.logged=true;
        this.setState(this.state);
    }
    onLogout = () => {
        ajaxWrapper.webToken=null;
        this.state.logged=false;
        this.setState(this.state);
    }

    render() {
        return (
            <MuiThemeProvider>
                <div>
                        <Header
                            logged={this.state.logged}
                            onLogout={this.onLogout}
                            ctx={this.state.ctx}
                            onSelectedChallengeChanged={this.onSelectedChallengeChanged}
                        />


                        { this.state.logged
                                ?
                                <LoggedView ctx={this.state.ctx}
                                            selectedChallengeDTO={this.state.selectedChallenge}
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

//start node server.js
