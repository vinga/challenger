import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import RaisedButton from "material-ui/RaisedButton";
import TextFieldExt from "../../views/common-components/TextFieldExt.tsx";
import LinearProgress from "material-ui/LinearProgress";
import FlatButton from 'material-ui/FlatButton';
import {loginUserAction} from "../accountActions";
import {REGISTER_SHOW_REGISTRATION_PANEL} from "../accountActionTypes";


interface ReduxProps {
    loginFailed: boolean,
    inProgress: boolean,
    errorDescription : string

};
interface ReactProps {
    infoDescription?: string | JSX.Element,
    currentLogin?: string,
    currentPass?: string,
    registerButtonVisible?: boolean
};
interface PropsFunc {
    onLoginFunc: (login: string, pass: string)=>void;
    onRegisterFunc: ()=>void;
};

class LoginPanelInternal extends React.Component<ReduxProps & ReactProps & PropsFunc, void> {
    private loginField:TextFieldExt;
    private passwordField:TextFieldExt;
    public static defaultProps: ReactProps = {
       registerButtonVisible: true
    };


    onLogin = () => {
        var login = this.loginField.state.fieldValue;
        var pass = this.passwordField.state.fieldValue;
        this.props.onLoginFunc(login,pass);
    };


    render() {
        //var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 200) + "px";
        return (
            <div id="main" className="container " >
                <div className="section ">

                    {this.props.loginFailed ?
                        <div className="row valign" style={{height: '100px'}}>
                            <div className="col s3 offset-s4">

                                <p className="grey-text">
                                    There is problem with logging:<br/>
                                    <b className="red-text text-darken-3">{this.props.errorDescription}</b>
                                </p>
                            </div>
                        </div>
                        : <div className="row" style={{height: '100px'}}></div>
                    }

                    <div className="row">
                        {this.props.infoDescription &&

                                <div className="col s3 offset-s4">
                                    <p className="grey-text">
                                        <b>{this.props.infoDescription}</b>
                                    </p>
                                </div>


                        }

                        <div className="col s3 offset-s4">
                            <TextFieldExt

                                floatingLabelText="Login"
                                fieldValue={this.props.currentLogin!=null? this.props.currentLogin: "kami"}
                                ref={(c)=>{this.loginField=c}}/>
                            <br />
                            <TextFieldExt
                                floatingLabelText="Password"
                                fieldValue={this.props.currentPass!=null? this.props.currentPass: "kamipass"}
                                type="password"
                                ref={(c)=>{this.passwordField=c}}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col s1 offset-s3">
                        </div>
                        <div className="col s3 " style={{paddingRight: '50px'}}>
                            <RaisedButton
                                label="Login"
                                fullWidth={true}
                                primary={true}
                                className="right" onClick={this.onLogin}/>
                            {this.props.inProgress && <LinearProgress mode="indeterminate"/> }
                            {this.props.registerButtonVisible &&
<div className="right">
                            <FlatButton label="Register" onClick={this.props.onRegisterFunc}/></div> }
                        </div>
                    </div>


                </div>
            </div>);
    }
}

const mapStateToProps = (state: ReduxState, props:ReactProps):any => {
    var errorDescription=state.accounts.filter(u=>u.primary==true).map(u=>{
        return u.errorDescription;
    }).pop();
    var inProgress=state.accounts.filter(u=>u.primary==true).map(u=>{
        return u.inProgress;
    }).pop();
    if (inProgress==undefined)
        inProgress=false;

    return {
        errorDescription: errorDescription,
        inProgress: inProgress,
        loginFailed: errorDescription!=null

    }
};

const mapDispatchToProps = (dispatch):any => {
    return {
        onLoginFunc: (login, pass) => {
            dispatch(loginUserAction(login, pass, true))
        },
        onRegisterFunc: ()  => {
            dispatch(REGISTER_SHOW_REGISTRATION_PANEL.new({}))
        }
    }
};


export const LoginPanel = connect(
        mapStateToProps,
        mapDispatchToProps
    )(LoginPanelInternal);

