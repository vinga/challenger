import React, {Component} from "react";
import Popover from "material-ui/Popover";
import TextField from "material-ui/TextField";
import ChallengeIcon from "./common-components/ChallengeIcon.tsx";
import FlatButton from "material-ui/FlatButton";

export default class SecondUserAuthorizePopover extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            open: false
        };
    }

    handleTouchTap = (event) => {
        // This prevents ghost click.
        event.preventDefault();

        this.setState({
            open: true,
            anchorEl: event.currentTarget,
        });
    };

    handleRequestClose = () => {
        this.setState({
            open: false,
        });
    };

    componentDidUpdate() {
        //console.log("FOCUS NOW");
        //ReactDOM.findDOMNode(this.refs.passInput).focus();
    }

    render() {
        return (<Popover
            open={this.state.open}
            anchorEl={this.state.anchorEl}
            anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'left', vertical: 'top'}}
            onRequestClose={this.handleRequestClose}
            ref="popover"
        >
            <div className="margined10">
                <div>
                    <ChallengeIcon icon="fa-key" style={{marginRight: '5px'}}/>
                    Please authorize as <b>{this.props.userName}</b>:
                </div>

                <div style={{display: 'block'}}>
                    <TextField
                        autoFocus
                        className="noShadow"
                        hintText="Password Field"
                        floatingLabelText="Password"
                        type="password"
                    />
                    <br/>
                </div>

                <div style={{marginBottom: '20px'}}>


                    <FlatButton
                        className="right"
                        onTouchTap={this.handleRequestClose}
                        label="Cancel"
                    />
                    <FlatButton
                        className="right"
                        onTouchTap={this.handleRequestClose}
                        label="OK"
                    />
                </div>
            </div>
        </Popover>);
    }
}