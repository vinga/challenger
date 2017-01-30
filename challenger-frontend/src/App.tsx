import * as React from "react";
import {ReduxState} from "./redux/ReduxState";
import {Provider, connect} from "react-redux";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import {Header} from "./views/Header";
import LoggedView from "./views/LoggedView";
import store from "./redux/store";
import {LOGOUT, LoginPanel, RegisterPanel, loggedUserSelector} from "./module_accounts/index";
import {GlobalPopover} from "./views/common-components/GlobalPopover";
import {ConfirmationPanel} from "./module_accounts/components/ConfirmationPanel";
import {GlobalWebCallProgress} from "./views/GlobalWebCallProgress";
import {CustomNotificationPanel} from "./views/CustomNotificationPanel";
import {Dialog, FlatButton} from "material-ui";
import {refetchChallengeData} from "./module_challenges/challengeActions";
import {CLOSE_TRY_AGAIN_WINDOW} from "./redux/actions/actions";


//2.0.3
//2.1.0-dev.20161023 try newer for object spread operator support

interface Props {
    logged: boolean,
    registering: boolean,
    confirmationLink: boolean,
    noInternetConnection?: boolean
}
interface PropsFunc {
    onLogout: () => void,
    onTryAgainWhenCommunicationProblem: () => void
}


const App = (props: Props & PropsFunc) => {

    const onTryAgain = () => {
        props.onTryAgainWhenCommunicationProblem();
    }
    return <MuiThemeProvider>
        <div>
            <GlobalWebCallProgress/>
            <Header logged={props.logged} onLogout={props.onLogout}/>
            <CustomNotificationPanel/>
            {
                props.registering ?
                    <RegisterPanel/>:
                    (props.logged ?
                        <LoggedView/>  :
                        (props.confirmationLink ? <ConfirmationPanel/>
                            :
                            <LoginPanel/> )) }
            <GlobalPopover/>

            {props.noInternetConnection &&
            <Dialog open={true}
                    title="Warning"
                    actions={ [<FlatButton
                                label="Try again"
                                primary={true}
                                onTouchTap={onTryAgain}
                            />]}
            >
                No internet connection. Something went wrong
            </Dialog>

            }
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
    var c = "action=";
    var cl = window.location.hash;
    return {
        confirmationLink: (window.location.hash.substr(1) || "").startsWith(c) || (state.confirmationLinkState != null && state.confirmationLinkState.uid != null),
        logged: loggedUserSelector(state) != null,
        registering: state.registerState != null,
        noInternetConnection: state.currentSelection.noInternetConnection
        //snackbarString: null//state.currentSelection.snackbarInfo
    }
};

const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onLogout: () => dispatch(LOGOUT.new({})),
        onTryAgainWhenCommunicationProblem: () => {
            dispatch(refetchChallengeData());
            dispatch(CLOSE_TRY_AGAIN_WINDOW.new({}))
        }
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


