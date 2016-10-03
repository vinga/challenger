import * as React from "react";
import Popover from "material-ui/Popover";
import TextField from "material-ui/TextField";
import ChallengeIcon from "../common-components/ChallengeIcon.tsx";
import FlatButton from "material-ui/FlatButton";
import {connect} from "react-redux";
import {ReduxState} from "../../redux/ReduxState";
import {loginUserAction} from "../../redux/actions/userActions";
import {AccountDTO} from "../../logic/domain/AccountDTO";
import ComponentDecorator = ReactRedux.ComponentDecorator;

interface Props {
    user:AccountDTO,
    anchorEl?:React.ReactInstance;
    handleRequestClose:()=>void;

}
interface PropsFunc {
    doLoginFunc:(login:string, password:string)=>(any); // shouldn't be optional
}


const mapStateToProps = (state:ReduxState, ownprops:Props)=> {
    return {}
};
const mapDispatchToProps = (dispatch) => {
    return {
        doLoginFunc: (login:string, password:string) => {
            dispatch(loginUserAction(login, password, false));
        }
    }
}

//@connect(mapStateToProps, mapDispatchToProps)
class SecondUserAuthorizePopover extends React.Component<{}& Props&PropsFunc,void> {
    passwordField:TextField;
    po:Popover;

    constructor(props) {
        super(props);


    }

    /*   handleRequestClose = (event) => {
     event.preventDefault();
     this.props.handleRequestClose();
     };
     */
    handleLogin = () => {
        this.props.doLoginFunc(this.props.user.login, this.passwordField.getValue());
        this.props.handleRequestClose();
    }

    render() {

        return (<Popover
            open={this.props.anchorEl!=null}
            anchorEl={this.props.anchorEl}
            anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'left', vertical: 'top'}}
            onRequestClose={this.props.handleRequestClose}
            ref={c=>this.po=c}
        >
            <div className="margined10">
                <div>
                    <ChallengeIcon icon="fa-key" style={{marginRight: '5px'}}/>
                    Please authorize as <b>{this.props.user.label}</b>:
                </div>

                <div style={{display: 'block'}} className="noShadow">
                    <TextField
                        autoFocus={true}
                        ref={(c)=>{this.passwordField=c;}}
                        defaultValue="jackpass"
                        hintText="Password Field"
                        floatingLabelText="Password"
                        type="password"
                    />
                    <br/>
                </div>

                <div style={{marginBottom: '10px'}} className="right">

                    <FlatButton

                        onTouchTap={this.handleLogin}
                        label="OK"
                    />
                    <FlatButton

                        onTouchTap={this.props.handleRequestClose}
                        label="Cancel"
                    />

                </div>
            </div>
        </Popover>);
    }
}

let Ext = connect(mapStateToProps, mapDispatchToProps)(SecondUserAuthorizePopover)
export default Ext;

