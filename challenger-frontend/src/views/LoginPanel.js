import React, {Component} from "react";
import RaisedButton from "material-ui/RaisedButton";
import TextFieldExt from "../common-components/TextFieldExt";
import LinearProgress from "material-ui/LinearProgress";
import ajaxWrapper from "../logic/AjaxWrapper";
import {loginUserAction} from "../redux/actions/usersActions";
import store from "../redux/store";

class LoginPanel extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            inProgress: false,
            loginFailed: false,
            errorDescription: ""
        }
    }

    onLogin = () => {
        var login = this.refs.loginField.state.fieldValue;
        var pass = this.refs.passwordField.state.fieldValue;
        this.props.onLoginFunc(login,pass);

        if (true)
            return;

        this.state.inProgress = true;
        this.setState(this.state);

        store.dispatch(loginUserAction(login,pass, true));

        ajaxWrapper.login(login, pass).then(
            (data)=> {
                this.props.onLoggedJWT(login, data)
            },
            (jqXHR, exception) => {
                this.state.inProgress = false;
                this.state.errordDescription = jqXHR.responseText;
                this.setState({state: this.state});

                if (jqXHR.status === 0) {
                    this.setState({
                        loginFailed: true,
                        errorDescription: "Connection refused"
                    });
                    // alert('Not connect.\n Verify Network.');
                } else if (jqXHR.status == 401) {
                    this.setState({
                        loginFailed: true,
                        errorDescription: jqXHR.responseText
                    });
                } else {
                    console.log("Error... " + jqXHR.status + " " + jqXHR.responseText);
                    this.setState({
                        loginFailed: true,
                        errorDescription: "Unexpected problem"
                    });
                }
            }
        );
    }


    render() {
        var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 200) + "px";

        return (
            <div id="main" className="container " >
                <div className="section ">

                    {!this.props.inProgress && this.props.loginFailed ?
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
                                style={{widht: '100%'}}
                                floatingLabelText="Login"
                                fieldValue="kami"
                                ref="loginField"/>
                            <br />
                            <TextFieldExt
                                floatingLabelText="Password"
                                fieldValue="kamipass"
                                type="password"
                                ref="passwordField"/>

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
                            {this.state.inProgress && <LinearProgress mode="indeterminate"/> }

                        </div>
                    </div>


                </div>
            </div>);
    }
}

const mapStateToProps = (state) => {
    console.log("changed.ST..");
console.log(state);
    var errorDescription=state.users.filter(u=>u.primary==true).map(u=>{
        return u.errorDescription;
    })
    var inProgress=state.users.filter(u=>u.primary==true).map(u=>{
        return u.inProgress;
    })
    var loginFailed=state.users.filter(u=>u.primary==true).map(u=>{
        return u.jwtToken==null;
    })


    return {
        errorDescription: errorDescription,
        inProgress: inProgress

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
