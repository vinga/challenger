import * as React from "react";
import RaisedButton from "material-ui/RaisedButton";
import TextFieldExt from "./common-components/TextFieldExt.tsx";
import LinearProgress from "material-ui/LinearProgress";
import {loginUserAction} from "../redux/actions/userActions";


interface Props {
    loginFailed: boolean,
    inProgress: boolean,
    errorDescription : string
}
interface PropsFunc {
    onLoginFunc: (login: string, pass: string)=>void;
}

class LoginPanel extends React.Component<Props & PropsFunc, void> {
    private loginField:TextFieldExt;
    private passwordField:TextFieldExt;
    constructor(props) {
        super(props);
    }

    onLogin = () => {
        var login = this.loginField.state.fieldValue;
        var pass = this.passwordField.state.fieldValue;
        this.props.onLoginFunc(login,pass);
    }


    render() {
        var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 200) + "px";
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
                        <div className="col s3 offset-s4">
                            <TextFieldExt
                                style={{width: '100%'}}
                                floatingLabelText="Login"
                                fieldValue="kami"
                                ref={(c)=>{this.loginField=c}}/>
                            <br />
                            <TextFieldExt
                                floatingLabelText="Password"
                                fieldValue="kamipass"
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

                        </div>
                    </div>


                </div>
            </div>);
    }
}

const mapStateToProps = (state) => {
    var errorDescription=state.users.filter(u=>u.primary==true).map(u=>{
        return u.errorDescription;
    }).pop();
    var inProgress=state.users.filter(u=>u.primary==true).map(u=>{
        return u.inProgress;
    }).pop();
    if (inProgress==undefined)
        inProgress=false;

    return {
        errorDescription: errorDescription,
        inProgress: inProgress,
        loginFailed: errorDescription!=null

    }
}
const mapDispatchToProps = (dispatch) => {
    return {
        onLoginFunc: (login, pass) => {
            dispatch(loginUserAction(login, pass, true))
        }
    }
}
import { connect } from 'react-redux'
const Ext = connect(
        mapStateToProps,
        mapDispatchToProps
    )(LoginPanel)

export default Ext;
