import * as React from "react";
import Popover from "material-ui/Popover";
import IconButton from "material-ui/IconButton";
import IconChooserGrid from "./IconChooserGrid";


interface Props {
    icon: string,
    onClick: (icon: string)=>void;
    disabled?: boolean
}
interface State {
    icon: string,
    open: boolean,
    anchorEl?: any;
}
export default class IconChooserButton extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        this.state = {
            icon: this.props.icon,
            open: false
        }
    }

    showPopover = (event: React.MouseEvent<any>) => {
        // This prevents ghost click.
        event.preventDefault();
        if (this.props.disabled != true) {
            this.setState({
                icon: this.state.icon,
                open: true,
                anchorEl: event.currentTarget,
            });
        }
    };

    handleRequestClose = () => {
        this.setState({
            icon: this.state.icon,
            open: false,
        });
    };
    onIconChoosen = (icon: string) => {

        this.props.onClick(icon);
        this.handleRequestClose();
    };

    render() {
        return (<div><IconButton
disabled={this.props.disabled}

                style={{fontSize:"30px",width: 70, height: 70}}
iconStyle={{color: this.props.disabled?"grey":"black"}}
                                 onClick={this.showPopover}>
                <i className={'fa ' + this.props.icon }
                         style={{fontSize: '30px', textAlign: 'center'}}></i>
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
                    <IconChooserGrid onClick={ this.onIconChoosen }/>
                </Popover>
            </div>

        );
    }
}