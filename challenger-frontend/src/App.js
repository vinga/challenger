import React, {Component} from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import Header from "./Header";
import jwtDecode from "jwt-decode";
import LoginPanel from "./LoginPanel";
import LoggedView from "./LoggedView";

$(function() {
    $.ajaxSetup({
        error: function(jqXHR, exception) {

            console.log("Error "+jqXHR.status+" "+jqXHR.responseText);


    /*        if (jqXHR.status === 0) {
                alert('Not connect.\n Verify Network.');
            } else if (jqXHR.status == 404) {
                alert('Requested page not found. [404]');
            } else if (jqXHR.status == 500) {
                alert('Internal Server Error [500].');
            } else if (jqXHR.status == 401) {
                console.log("UNAUTHORIZED [401]");
            } else if (exception === 'parsererror') {
                alert('Requested JSON parse failed.');
            } else if (exception === 'timeout') {
                alert('Time out error.');
            } else if (exception === 'abort') {
                alert('Ajax request aborted.');
            } else {
                alert('Uncaught Error.\n' + jqXHR.responseText);
            }*/
        }
    });
});



var contextDTO = {
    baseUrl: "http://localhost:9080/api",
    userColors: ["#ffcc80", "#80deea"],
    userColorsDarken3: ["#ef6c00", "#00838f"],
    userColorsTextClass: ["orange-text", "cyan-text"],
    logged: false,
    user1: {
        userName: "unknown"
    },
    userId: undefined,
    selectedContract: undefined,
    contractId: -1,
    myLabel:"",
    hisLabel:""
}



export default class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            ctx: contextDTO

        };

    }

    onSelectedContractChanged = (contractDTO) => {
        this.state.ctx.selectedContract=contractDTO;
        this.state.ctx.contractId=contractDTO.id;
        this.state.ctx.myLabel=contractDTO.myId==contractDTO.firstUserId ? contractDTO.firstUserLabel: contractDTO.secondUserLabel;
        this.state.ctx.hisLabel=contractDTO.myId!=contractDTO.firstUserId ? contractDTO.firstUserLabel: contractDTO.secondUserLabel;

        this.setState(this.state);
    }

    onLoggedJWT = (login, webToken) => {

        var decoded = jwtDecode(webToken);
        this.state.ctx.userId=decoded.info.userId;
        this.state.ctx.webToken=webToken;
        this.state.ctx.logged=true;
        this.state.ctx.user1.userName=login;
        this.setState(this.state);
    }
    onLogout = () => {
        this.state.ctx.logged=false;
        this.setState(this.state);
    }


    render() {


        return (
            <MuiThemeProvider>
                <div>


                        <Header
                            userName={this.state.ctx.user1.userName}
                            logged={this.state.ctx.logged}
                            onLogout={this.onLogout}
                            ctx={this.state.ctx}
                            onSelectedContractChanged={this.onSelectedContractChanged}
                        />


                        { (this.state.ctx.logged === true)
                                ?
                                <LoggedView ctx={this.state.ctx} />
                                :
                                <LoginPanel
                                    ctx={this.state.ctx}
                                    onLoggedJWT={this.onLoggedJWT}/>
                        }


                  {/*  <Footer/>*/}
                </div>
            </MuiThemeProvider >
        );
    }
}

//start node server.js
