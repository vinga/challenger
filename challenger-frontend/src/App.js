import React, {Component} from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import Header from "./views/Header";
import jwtDecode from "jwt-decode";
import LoginPanel from "./views/LoginPanel";
import LoggedView from "./views/LoggedView";
import ajaxWrapper from "./presenters/AjaxWrapper"

ajaxWrapper.baseUrl= "http://localhost:9080/api";

var contextDTO = {
    userColors: ["#ffcc80", "#80deea"],
    userColorsDarken3: ["#ef6c00", "#00838f"],
    userColorsTextClass: ["orange-text", "cyan-text"],
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
            selectedContract: null
        };
    }

    onSelectedContractChanged = (contractDTO) => {
        this.state.selectedContract=contractDTO;
        this.state.ctx.me.label=contractDTO.myId==contractDTO.firstUserId ? contractDTO.firstUserLabel: contractDTO.secondUserLabel;
        this.state.ctx.him.label=contractDTO.myId!=contractDTO.firstUserId ? contractDTO.firstUserLabel: contractDTO.secondUserLabel;
        this.state.ctx.me.id=contractDTO.myId;
        this.state.ctx.him.id=contractDTO.myId==contractDTO.firstUserId? contractDTO.secondUserId: contractDTO.firstUserId;

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
                            onSelectedContractChanged={this.onSelectedContractChanged}
                        />


                        { this.state.logged
                                ?
                                <LoggedView ctx={this.state.ctx}
                                            selectedContract={this.state.selectedContract}
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
