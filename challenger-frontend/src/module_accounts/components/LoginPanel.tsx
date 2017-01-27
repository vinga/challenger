import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import RaisedButton from "material-ui/RaisedButton";
import TextFieldExt from "../../views/common-components/TextFieldExt";
import LinearProgress from "material-ui/LinearProgress";
import FlatButton from "material-ui/FlatButton";
import {loginUserAction, sendResetPasswordLinkAction} from "../accountActions";
import {REGISTER_SHOW_REGISTRATION_PANEL, START_FORGOT_PASSWORD_MODE, REGISTER_EXIT_TO_LOGIN_PANEL, FINISH_FORGOT_PASSWORD_MODE} from "../accountActionTypes";
import {Col, Row, RowCol} from "../../views/common-components/Flexboxgrid";
import {ForgotPasswordPanel} from "./ForgotPasswordPanel";
import {FontIcon} from "material-ui";


interface ReduxProps {
    loginFailed: boolean,
    inProgress: boolean,
    errorDescription: string,
    infoDescription: string,
    forgotPasswordMode: boolean,


}

interface ReactProps {
    infoDescription?: string | JSX.Element,
    currentLogin?: string,
    currentPass?: string,
    registerButtonVisible?: boolean
}

interface PropsFunc {
    onLoginFunc: (login: string, pass: string)=>void;
    onRegisterFunc: ()=>void;
    onForgotPassword: ()=>void;
    onForgotPasswordSendConfirmationLink: (email: string) => void;
    onExitToLoginFunc: ()=>void;
}


interface DevState {
    login: string
}
class LoginPanelInternal extends React.Component<ReduxProps & ReactProps & PropsFunc, DevState> {
    private loginField: TextFieldExt;
    private passwordField: TextFieldExt;
    public static defaultProps: ReactProps = {
        registerButtonVisible: true
    };
    state= {login: "kami"}

    onLogin = () => {
        var login = this.loginField.state.fieldValue;
        this.setState({login})
        var pass = this.passwordField.state.fieldValue;
        this.props.onLoginFunc(login, pass);
    };

    onLoginGoogle = () => {
        location.href = "/oauth2/googleSignIn";
    }
    onLoginFacebook = () => {
        location.href = "/oauth2/facebookSignIn";
    }


    render() {
        if (this.props.forgotPasswordMode) {
            return <ForgotPasswordPanel
                onForgotPassword={this.props.onForgotPasswordSendConfirmationLink}
                onExitToLoginFunc={this.props.onExitToLoginFunc}/>
        }

        //var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 200) + "px";
        return (
            <div id="main" className="container ">

                <Row horizontal="center" style={{minHeight: '130px', paddingTop:'30px'}}>
                    {this.props.loginFailed &&
                    <Col col="8">
                        <p className="grey-text">
                            There is problem with logging:<br/>
                            <b className="red-text text-darken-3">{this.props.errorDescription}</b>
                        </p>
                    </Col> }
                    {this.props.infoDescription &&
                    <Col col="8">
                        <p className="grey-text"><b>{this.props.infoDescription}</b></p>
                    </Col> }
                </Row>


                <Row horizontal="center">
                    <Col col="8-6-3" style={{minWidth:"250px",padding:"20px"}}>
                        <RowCol>
                            <TextFieldExt
                                autoFocus={true}
                                fullWidth={true}
                                floatingLabelText="Login"
                                fieldValue={this.props.currentLogin!=null? this.props.currentLogin: this.state.login}
                                onEnterKeyDown={()=> {this.passwordField.focus(); } }
                                ref={(c)=>{this.loginField=c}}/>
                        </RowCol>
                        <RowCol>
                            <TextFieldExt

                                fullWidth={true}
                                floatingLabelText="Password"
                                fieldValue={this.props.currentPass!=null? this.props.currentPass: "passpass"}
                                onEnterKeyDown={this.onLogin}
                                type="password"
                                ref={(c)=>{this.passwordField=c}}/>
                        </RowCol>
                        <RowCol colStyle={{paddingTop: '30px', paddingBottom: '30px'}}>
                            <div style={{display:"block"}}>
                                <RaisedButton
                                    fullWidth={true}
                                    label="Login"
                                    primary={true}
                                    className="right" onClick={this.onLogin}/>
                                {this.props.inProgress && <LinearProgress mode="indeterminate"/> }
                            </div>
                        </RowCol>
                        <RowCol horizontal="end">
                            {this.props.registerButtonVisible &&
                            <FlatButton label="Register" onClick={this.props.onRegisterFunc}/>}

                        </RowCol>
                        <RowCol horizontal="end">
                            <FlatButton labelStyle={{textTransform:"none", color:"#888888"}} label="Forgot password?" onClick={this.props.onForgotPassword}/>
                        </RowCol>




                    </Col>

                    <Col col="8-6-3" style={{minWidth:"250px",padding:'20px',paddingTop:'50px'}}>
                        <RowCol colStyle={{ paddingTop: '30px', paddingBottom: '0px'}}>
                            <div style={{display:"block"}}>
                                <RaisedButton
                                    fullWidth={true}
                                    label="Login with Google"
                                    className="right"
                                    labelColor="white"
                                    backgroundColor="#D34836"
                                    onClick={this.onLoginGoogle}
                                    icon={<FontIcon

                                className={"fa fa-google"}/>}
                                />
                                {this.props.inProgress && <LinearProgress mode="indeterminate"/> }
                            </div>
                        </RowCol>

                        <RowCol colStyle={{paddingTop: '30px', paddingBottom: '30px'}}>
                            <div style={{display:"block"}}>
                                <RaisedButton
                                    fullWidth={true}
                                    label="Login with Facebook"

                                    className="right"
                                    labelColor="white"
                                    backgroundColor="#3b5998"
                                    onClick={this.onLoginFacebook}
                                    icon={<FontIcon

                                className={"fa fa-facebook"}/>}
                                />
                                {this.props.inProgress && <LinearProgress mode="indeterminate"/> }
                            </div>
                        </RowCol>
                    </Col>
                </Row>

            </div>);
    }
}

const mapStateToProps = (state: ReduxState, props: ReactProps): any => {
    var errorDescription = state.currentSelection.loginErrorDescription

    var inProgress = state.accounts.filter(u=>u.primary == true).map(u=> {
        return u.inProgress;
    }).pop();
    if (inProgress == undefined)
        inProgress = false;

    var user = state.accounts.find(u=>u.primary == true);
    var infoDescription = props.infoDescription;
    if (user != null && user.infoDescription != null)
        infoDescription = user.infoDescription;
    if (infoDescription==null)
        infoDescription=state.currentSelection.loginInfoDescription
    return {
        errorDescription: errorDescription,
        infoDescription: infoDescription,
        forgotPasswordMode: state.currentSelection.forgotPasswordMode == true,
        inProgress: inProgress,
        loginFailed: errorDescription != null

    }
};

const mapDispatchToProps = (dispatch): any => {
    return {
        onLoginFunc: (login, pass) =>
            dispatch(loginUserAction(login, pass, true))
        ,
        onRegisterFunc: () =>
            dispatch(REGISTER_SHOW_REGISTRATION_PANEL.new({}))
        ,
        onForgotPassword: () =>
            dispatch(START_FORGOT_PASSWORD_MODE.new({})),

        onForgotPasswordSendConfirmationLink: (email: string) =>
            dispatch(sendResetPasswordLinkAction(email)),
        onExitToLoginFunc: () => {
            dispatch(FINISH_FORGOT_PASSWORD_MODE.new({emailSent: false}));
            dispatch(REGISTER_EXIT_TO_LOGIN_PANEL.new({}));
        }

    }
};


export const LoginPanel = connect(
    mapStateToProps,
    mapDispatchToProps
)(LoginPanelInternal);

