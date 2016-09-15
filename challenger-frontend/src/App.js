import React, {Component} from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import Header from "./views/Header";
import jwtDecode from "jwt-decode";
import LoginPanel from "./views/LoginPanel";
import LoggedView from "./views/LoggedView";
import ajaxWrapper from "./logic/AjaxWrapper";
import store from "./redux/store";
import {Provider} from "react-redux";
import {logout} from "./redux/actions/users"
import TypeCompa from "./TypeCompa.tsx";
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
    }

    render() {
        return (
            <MuiThemeProvider>
                    <div>
                        <Header
                            logged={this.props.logged}
                            onLogout={this.props.onLogout}
                        />
                        <TypeCompa/>
                        { this.props.logged
                            ?
                            <LoggedView/>
                            :
                            <LoginPanel/>
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
        day: state.mainReducer.day
    }
}
const mapDispatchToProps = (dispatch) => {
    return {
        onLogout: (login) => {
            dispatch(logout())
        }
    }
}

import { connect } from 'react-redux'
let ConnectedApp = connect(mapStateToProps, mapDispatchToProps)(App)

let ProvidedApp = (serverProps) => {
    return (
        <Provider store={store}>
            <ConnectedApp/>
        </Provider>
    )
}
export default ProvidedApp;

/*import {t1} from "./hello"
console.log("typescript example");
console.log(t1);*/
//start node server.js
