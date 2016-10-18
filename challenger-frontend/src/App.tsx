import * as React from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import Header from "./views/Header.tsx";
import LoginPanel from "./views/LoginPanel.tsx";
import LoggedView from "./views/LoggedView.tsx";
import store from "./redux/store.ts";
import {Provider, connect} from "react-redux";
import {ReduxState} from "./redux/ReduxState";
import {AccountDTO} from "./logic/domain/AccountDTO";

// typescript 2.0.3 caused errors

interface Props {
    logged:boolean
}


const App = (props:Props)=> {
    return <MuiThemeProvider>
        <div>
            <Header/> { props.logged ?   <LoggedView/>  :  <LoginPanel/>  }
        </div>
    </MuiThemeProvider >
};



const mapStateToProps = (state:ReduxState):Props => {
    var logged = state.users.filter(u=>u.primary == true).map((u:AccountDTO)=> {
        return u.jwtToken != null;
    }).pop();
    return {
        logged: logged,
    }
}


let ConnectedApp = connect(mapStateToProps)(App)

let ProvidedApp = () => {
    return (
        <Provider store={store}>
            <ConnectedApp/>
        </Provider>
    )
}
export default ProvidedApp;


