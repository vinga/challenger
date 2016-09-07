import React, {Component} from "react";
import Popover from "material-ui/Popover";
import IconButton from "material-ui/IconButton";
import IconChooserGrid from "./IconChooserGrid";

export default class IconChooserButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            icon: this.props.icon,
            open: false
        }
    }

    showPopover  = (event) => {
        // This prevents ghost click.
        event.preventDefault();

        this.setState({
            open: true,
            anchorEl: event.currentTarget,
        });
    };

    handleRequestClose = () => {
        this.setState({open: false});
    }
    onIconChoosed = (icon) => {
        this.props.onClick(icon);
        this.handleRequestClose();
    }

    render() {
        return (<div><IconButton style={{width: 70, height: 70}}
                            onClick={this.showPopover}>
            &nbsp;<i className={'fa ' + this.props.icon }
                     style={{fontSize: '30px', color: "black", textAlign: 'center'}}></i>
        </IconButton>

            <Popover
                open={this.state.open}
                anchorEl={this.state.anchorEl}
                anchorOrigin={{horizontal: 'right', vertical: 'top'}}
                targetOrigin={{horizontal: 'left', vertical: 'top'}}
                onRequestClose={this.handleRequestClose}
                style={{width:'350px'}}
                ref="popover"
            >
                <IconChooserGrid onClick={ this.onIconChoosed }/>
            </Popover>
            </div>

    );
    }
}