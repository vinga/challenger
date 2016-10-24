import * as React from "react";
import {ReduxState} from "./redux/ReduxState";
import {Provider, connect} from "react-redux";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import {Header} from "./views/Header.tsx";
import LoggedView from "./views/LoggedView.tsx";
import store from "./redux/store.ts";
import {LOGOUT, LoginPanel, RegisterPanel, loggedUserSelector} from "./module_accounts/index";


//2.0.3
//2.1.0-dev.20161023 try newer for object spread operator support

interface Props {
    logged: boolean,
    registering: boolean,

}
interface PropsFunc {
    onLogout: ()=>void
}


const App = (props: Props & PropsFunc)=> {
    return <MuiThemeProvider>
        <div>
            <Header logged={props.logged} onLogout={props.onLogout}/> {
            props.registering ?
                <RegisterPanel/>:
                (props.logged ?
                    <LoggedView/>  :
                    <LoginPanel/> ) }
        </div>
    </MuiThemeProvider >
};


const mapStateToProps = (state: ReduxState): Props => {
    return {
        logged: loggedUserSelector(state)!=null,
        registering: state.registerState != null
    }
};

const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onLogout: () =>  dispatch(LOGOUT.new({}))
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


