import * as React from "react";
import Popover from "material-ui/Popover";
import IconButton from "material-ui/IconButton";
import IconChooserGrid from "./IconChooserGrid.tsx";


interface Props {
    icon:string,
    onClick:(icon:string)=>void;
}
interface State {
    icon:string,
    open:boolean,
    anchorEl?:any;
}
export default class IconChooserButton extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        this.state = {
            icon: this.props.icon,
            open: false
        }
    }

    showPopover = (event:Event) => {
        // This prevents ghost click.
        event.preventDefault();
        this.setState({
            icon: this.state.icon,
            open: true,
            anchorEl: event.currentTarget,
        });
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
                    <IconChooserGrid onClick={ this.onIconChoosen }/>
                </Popover>
            </div>

        );
    }
}