import * as React from "react";
import Popover from "material-ui/Popover";
import TextField from "material-ui/TextField";
import FlatButton from "material-ui/FlatButton";
import {AccountDTO} from "../AccountDTO";
import ChallengeIcon from "../../views/common-components/ChallengeIcon";


interface Props {
    user: AccountDTO,
    popoverAnchorEl?: React.ReactInstance;
    open: boolean,
    close: ()=>void,
    errorDescription?: string
}
interface PropsFunc {
    doLoginFunc: (login: string, password: string, userId: number)=>(any);
}



export class SecondUserAuthorizePopoverDialog extends React.Component<Props& PropsFunc,void> {
    passwordField: TextField;
    po: Popover;

    constructor(props) {
        super(props);
    }


    handleLogin = () => {
        this.props.doLoginFunc(this.props.user.login, this.passwordField.getValue(), this.props.user.id);
        //this.internalClose();
    };
    internalClose = () => {
        this.props.close();
    }

// {this.props.user.errorDescription != null &&
// <span className="red-text text-darken-3"
//     style={{fontSize:'15px'}}>
// {this.props.user.errorDescription}</span>}

    render() {


        if (this.props.user!=null && this.props.user.login==null)
            return <Popover
                anchorEl={this.props.popoverAnchorEl}
                anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
                targetOrigin={{horizontal: 'left', vertical: 'top'}}
                onRequestClose={this.internalClose}
                open={this.props.open}>

                <div className="margined10">
                    User account isn't active yet.
                </div>

            </Popover>;

        return (<Popover
            open={this.props.open}
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
                {/*autoFocus={true}*/}
                <div style={{display: 'block'}} className="noShadow">
                    <TextField

                        ref={(c)=>{this.passwordField=c;}}
                        defaultValue={typeof DEVELOPMENT_MODE !== "undefined"? "passpass": ""}
                        hintText="Password Field"
                        floatingLabelText="Password"
                        type="password"
                    />
                    <br/>
                </div>


                {this.props.errorDescription != null &&
                    <div>{this.props.errorDescription}</div>
                }
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

