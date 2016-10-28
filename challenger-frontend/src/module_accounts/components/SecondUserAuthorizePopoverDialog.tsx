import * as React from "react";
import Popover from "material-ui/Popover";
import TextField from "material-ui/TextField";
import FlatButton from "material-ui/FlatButton";
import {AccountDTO} from "../AccountDTO";
import ChallengeIcon from "../../views/common-components/ChallengeIcon";


interface Props {
    user: AccountDTO,
    popoverAnchorEl?: React.ReactInstance;
    close: ()=>void
}
interface PropsFunc {
    doLoginFunc: (login: string, password: string)=>(any);
}



export class SecondUserAuthorizePopoverDialog extends React.Component<Props& PropsFunc,void> {
    passwordField: TextField;
    po: Popover;

    constructor(props) {
        super(props);
    }


    handleLogin = () => {
        this.props.doLoginFunc(this.props.user.login, this.passwordField.getValue());
        this.internalClose();
    };
    internalClose = () => {
        this.props.close();
    }


    render() {

        return (<Popover
            open={this.props.popoverAnchorEl!=null}
            anchorEl={this.props.popoverAnchorEl}
            anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'left', vertical: 'top'}}
            onRequestClose={this.internalClose}
            ref={c=>this.po=c}
        >
            {this.props.user != null &&
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

                        onTouchTap={this.internalClose}
                        label="Cancel"
                    />

                </div>
            </div> }
        </Popover>);
    }
}

