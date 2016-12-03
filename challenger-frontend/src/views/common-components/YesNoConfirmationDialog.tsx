import * as React from "react";
import {Dialog} from "material-ui";
import {FlatButton} from "material-ui";

interface Props {

    closeYes: ()=>void;
    closeNo?: ()=> void;
    closeDialog: ()=>void;
    title?: string
    width?: string
    showCancel?: boolean
}

export class YesNoConfirmationDialog extends React.Component<Props, void> {
    constructor(props: Props) {
        super(props);
    }

    handleYes = () => {
        this.props.closeYes();
        this.props.closeDialog();
    };
    handleNo = () => {
        if (this.props.closeNo!=null)
            this.props.closeNo();
        this.props.closeDialog();
    };
    handleClose= () => {
       this.props.closeDialog();
    };

    render() {
        const actions = [
            <FlatButton
                label="Yes"
                primary={true}
                onTouchTap={this.handleYes}
            />,
            <FlatButton
                label="No"
                primary={false}
                onTouchTap={this.handleNo}
            />,
        ];
        if (this.props.showCancel) {
            actions.push(<FlatButton
                label="Cancel"
                primary={false}
                onTouchTap={this.handleClose}
            />);
        }

        return <Dialog open={true}
                       contentStyle={{width: this.props.width!=null? this.props.width: 400}}

      title={this.props.title}
        actions={actions}
        >
            {this.props.children}
        </Dialog>;
    }

}