import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import RaisedButton from "material-ui/RaisedButton";
import TextFieldExt from "../../views/common-components/TextFieldExt.tsx";
import {LoginPanel} from "./LoginPanel.tsx";
import LinearProgress from "material-ui/LinearProgress";
import {registerUserAction} from "../accountActions";

interface Props {
    registeredSuccessfully: boolean,
    requireEmailConfirmation: boolean,
    registerFailed: boolean,
    inProgress: boolean,
    errorDescription : string
}
interface PropsFunc {
    onRegisterFunc: (email: string, login: string, pass: string)=>void;
}
interface State {
    loginError?:string,
    emailError?:string,
    passwordError?:string,
}

class RegisterPanelInternal extends React.Component<Props & PropsFunc, State> {
    private loginField:TextFieldExt;
    private emailField:TextFieldExt;
    private passwordField:TextFieldExt;
    constructor(props) {
        super(props);
        this.state={}
    }

    onRegister = () => {
        if (this.validate()) {
            var login = this.loginField.state.fieldValue;
            var pass = this.passwordField.state.fieldValue;
            var email = this.emailField.state.fieldValue;
            this.props.onRegisterFunc(email, login, pass);
        }
    };

    validate = ():boolean => {

        return this.loginField.checkIsValid() && this.passwordField.checkIsValid() && this.emailField.checkIsValid();
    }

    render() {
        if (this.props.registeredSuccessfully) {
            if (this.props.requireEmailConfirmation)
                return <div id="main" className="container " >
                    <div className="section ">

                            <div className="row valign" style={{height: '100px'}}>
                                <div className="col s3 offset-s4">
                                      <h5>Registration finished.</h5>
                                      <h5>Please confirm your email address</h5>
                                </div></div>
                        </div>
                    </div>;

            return <LoginPanel
                infoDescription={<b>Registration finished.<br/> Please login to the system</b>}
                currentLogin={this.loginField.state.fieldValue}
                currentPass={this.passwordField.state.fieldValue}
                registerButtonVisible={false}
            />
        }
        //var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 200) + "px";
        return (
            <div id="main" className="container " >
                <div className="section ">

                    <div className="row">
                        <div className="row" style={{height: '100px'}}></div>



                        {this.props.registerFailed ?
                            <div className="row valign" style={{height: '100px'}}>
                                <div className="col s3 offset-s4">
                                    <p className="grey-text">
                                        There is problem with registration:<br/>
                                        <b className="red-text text-darken-3">{this.props.errorDescription}</b>
                                    </p>
                                </div>
                            </div>
                            :
                            <div className="col s3 offset-s4">
                                <h5>Register</h5>
                            </div>
                        }


                        <div className="col s3 offset-s4">
                            <TextFieldExt
                                floatingLabelText="Email"
                                useRequiredValidator={true}
                                validateOnChange={true}
                                minLengthNumber={4}
                                maxLengthNumber={100}
                                checkEmailPattern={true}
                                fieldValue="newUser22@email.com"
                                ref={(c)=>{this.emailField=c}}/>
                            <br />
                            <TextFieldExt
                                useRequiredValidator={true}
                                validateOnChange={true}
                                minLengthNumber={6}
                                maxLengthNumber={30}
                                floatingLabelText="Login"
                                fieldValue="kami22"
                                ref={(c)=>{this.loginField=c}}/>
                            <br />
                            <TextFieldExt
                                floatingLabelText="Password"
                                validateOnChange={true}
                                fieldValue="kamipass22"
                                type="password"
                                minLengthNumber={6}
                                maxLengthNumber={30}
                                useRequiredValidator={true}
                                ref={(c)=>{this.passwordField=c}}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col s1 offset-s3">
                        </div>
                        <div className="col s3" style={{paddingRight: '50px'}}>
                            <RaisedButton
                                label="Register"
                                fullWidth={true}
                                primary={true}
                                className="right" onClick={this.onRegister}/>
                            {this.props.inProgress && <LinearProgress mode="indeterminate"/> }

                        </div>
                    </div>


                </div>
            </div>);
    }
}

const mapStateToProps = (state:ReduxState) => {


    var errorDescription=state.registerState.registerError;
    var inProgress=state.registerState.registerInProgress;
    if (inProgress==undefined)
        inProgress=false;

    return {
        errorDescription: errorDescription,
        inProgress: inProgress,
        registerFailed: errorDescription!=null,
        registeredSuccessfully:  state.registerState.registeredSuccessfully

    }
};

const mapDispatchToProps = (dispatch) => {
    return {
        onRegisterFunc: (email, login, pass) => {
            dispatch(registerUserAction(email, login, pass))
        }
    }
};


export const RegisterPanel = connect(
    mapStateToProps,
    mapDispatchToProps
)(RegisterPanelInternal);

