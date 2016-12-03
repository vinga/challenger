import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import RaisedButton from "material-ui/RaisedButton";
import TextFieldExt from "../../views/common-components/TextFieldExt.tsx";
import LinearProgress from "material-ui/LinearProgress";
import FlatButton from "material-ui/FlatButton";
import {loginUserAction} from "../accountActions";
import {REGISTER_SHOW_REGISTRATION_PANEL} from "../accountActionTypes";
import {Col, Row, RowCol} from "../../views/common-components/Flexboxgrid";

interface ReduxProps {
    loginFailed: boolean,
    inProgress: boolean,
    errorDescription: string,
    infoDescription: string,


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
}


class LoginPanelInternal extends React.Component<ReduxProps & ReactProps & PropsFunc, void> {
    private loginField: TextFieldExt;
    private passwordField: TextFieldExt;
    public static defaultProps: ReactProps = {
        registerButtonVisible: true
    };


    onLogin = () => {
        var login = this.loginField.state.fieldValue;
        var pass = this.passwordField.state.fieldValue;
        this.props.onLoginFunc(login, pass);
    };


    render() {


        //var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 200) + "px";
        return (
            <div id="main" className="container ">


                <div className="section ">


                    <Row horizontal="center" style={{height: '100px'}}>
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
                        <Col col="8-5-3">
                            <RowCol>
                                <TextFieldExt
                                    fullWidth={true}
                                    floatingLabelText="Login"
                                    fieldValue={this.props.currentLogin!=null? this.props.currentLogin: "kami"}
                                    ref={(c)=>{this.loginField=c}}/>
                            </RowCol>
                            <RowCol>
                                <TextFieldExt
                                    fullWidth={true}
                                    floatingLabelText="Password"
                                    fieldValue={this.props.currentPass!=null? this.props.currentPass: "kamipass"}
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
                                Forgot password?
                            </RowCol>
                        </Col>

                    </Row>




                </div>
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
    return {
        errorDescription: errorDescription,
        infoDescription: infoDescription,

        inProgress: inProgress,
        loginFailed: errorDescription != null

    }
};

const mapDispatchToProps = (dispatch): any => {
    return {
        onLoginFunc: (login, pass) => {
            dispatch(loginUserAction(login, pass, true))
        },
        onRegisterFunc: () => {
            dispatch(REGISTER_SHOW_REGISTRATION_PANEL.new({}))
        }
    }
};


export const LoginPanel = connect(
    mapStateToProps,
    mapDispatchToProps
)(LoginPanelInternal);

