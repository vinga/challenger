import * as React from "react";
import {Dialog} from "material-ui";
import {FlatButton} from "material-ui";

interface Props {

    closeYes: ()=>void;
    closeDialog: ()=>void;
}

export default class YesNoConfirmationDialog extends React.Component<Props, void> {
    constructor(props: Props) {
        super(props);
    }

    handleYes = () => {
        this.props.closeYes();
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
                onTouchTap={this.handleClose}
            />,
        ];

        return <Dialog open={true}
                       contentStyle={{width:400}}
        actions={actions}
        >
            {this.props.children}
        </Dialog>;
    }

}