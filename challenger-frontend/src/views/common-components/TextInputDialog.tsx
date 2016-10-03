import * as React from "react";
import {Dialog} from "material-ui";
import {FlatButton} from "material-ui";
import TextFieldExt from "./TextFieldExt";

interface Props {
    floatingLabelText: string,
    closeYes: (str: string)=>void;
    closeDialog: ()=>void;
}

export default class TextInputDialog extends React.Component<Props, void> {
    textField: TextFieldExt;
    constructor(props: Props) {
        super(props);
    }

    handleYes = (str) => {
        this.props.closeYes(this.textField.state.fieldValue);
        this.props.closeDialog();
    }
    handleClose= () => {
       this.props.closeDialog();
    }

    render() {
        const actions = [
            <FlatButton
                label="Ok"
                primary={true}
                onTouchTap={this.handleYes}
            />,
            <FlatButton
                label="Cancel"
                primary={false}
                onTouchTap={this.handleClose}
            />,
        ];

        return <Dialog open={true}
                       contentStyle={{width:600}}
        actions={actions}
        >
           <TextFieldExt    autoFocus={true} style={{width:"100%"}} ref={c=>this.textField=c} floatingLabelText={this.props.floatingLabelText} />
        </Dialog>;
    }

}