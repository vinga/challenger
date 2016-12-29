import * as React from "react";
import {ReduxState} from "./redux/ReduxState";
import {Provider, connect} from "react-redux";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import {Header} from "./views/Header.tsx";
import LoggedView from "./views/LoggedView.tsx";
import store from "./redux/store.ts";
import {LOGOUT, LoginPanel, RegisterPanel, loggedUserSelector} from "./module_accounts/index";
import {GlobalPopover} from "./views/common-components/GlobalPopover";
import {ConfirmationPanel} from "./module_accounts/components/ConfirmationPanel";
import {GlobalWebCallProgress} from "./views/GlobalWebCallProgress";


//2.0.3
//2.1.0-dev.20161023 try newer for object spread operator support

interface Props {
    logged: boolean,
    registering: boolean,
    confirmationLink: boolean,
}
interface PropsFunc {
    onLogout: ()=>void
}


const App = (props: Props & PropsFunc)=> {

    return <MuiThemeProvider>
        <div>
            <GlobalWebCallProgress/>
            <Header logged={props.logged} onLogout={props.onLogout}/> {
            props.registering ?
                <RegisterPanel/>:
                (props.logged ?
                    <LoggedView/>  :
                    (props.confirmationLink ? <ConfirmationPanel/>
                        :
                        <LoginPanel/> )) }
            <GlobalPopover/>

            {/* <Snackbar
             open={props.snackbarString!=null && props.snackbarString!=""}
             message={props.snackbarString !=null ?props.snackbarString: "nothing"}
             autoHideDuration={4000}
             onRequestClose={()=>{}}
             />*/}
        </div>
    </MuiThemeProvider >
};


const mapStateToProps = (state: ReduxState): Props => {
    var c = "confirmation=";
    var cl = window.location.hash;
    return {
        confirmationLink: (window.location.hash.substr(1) || "").startsWith(c) || (state.confirmationLinkState != null && state.confirmationLinkState.uid != null),
        logged: loggedUserSelector(state) != null,
        registering: state.registerState != null,
        //snackbarString: null//state.currentSelection.snackbarInfo
    }
};

const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onLogout: () => dispatch(LOGOUT.new({})),
    }
};

let ConnectedApp = connect(mapStateToProps, mapDispatchToProps)(App);
let ProvidedApp = () => {
    return (
        <Provider store={store}>
            <ConnectedApp/>
        </Provider>
    )
};
export default ProvidedApp;


