import * as React from "react";
import {Dialog} from "material-ui";
import {FlatButton} from "material-ui";
import TextFieldExt from "./TextFieldExt";

interface Props {
    floatingLabelText: string,
    closeYes: (str: string)=>void;
    closeDialog: ()=>void;
    inputRequired?: boolean
}

export default class TextInputDialog extends React.Component<Props, void> {
    textField: TextFieldExt;
    constructor(props: Props) {
        super(props);
    }

    handleYes = () => {
        if (!this.textField.checkIsValid())
            return;
        var str=this.textField.state.fieldValue;
        this.props.closeYes(str);
        this.props.closeDialog();
    };
    handleClose= () => {
       this.props.closeDialog();
    };

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
           <TextFieldExt    autoFocus={true}
                            style={{width:"100%"}}
                            ref={c=>this.textField=c}
                            onEnterKeyDown={this.handleYes}
                            floatingLabelText={this.props.floatingLabelText}
                            useRequiredValidator={this.props.inputRequired==null || this.props.inputRequired==true}
                            validateOnChange={true}
           />
        </Dialog>;
    }

}
