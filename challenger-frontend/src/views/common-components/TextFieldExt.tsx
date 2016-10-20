import * as React from "react";
import TextField from "material-ui/TextField";
import CSSProperties = __React.CSSProperties;


interface Props {
    floatingLabelText: string,
    type? :string,
    fieldValue?: string,
    style?: CSSProperties;
    autoFocus?: boolean
}
interface State {
    fieldValue: string
}
export default class TextFieldExt extends React.Component<Props,State> {
    state = { fieldValue: this.props.fieldValue };
    static defaultProps = {
        type:"text",
        fieldValue:"",
        style: {}
    };


    handleFieldChange = (event) => {
        this.setState({fieldValue: event.target.value});
    };

    render() {
        return (<TextField
            autoFocus={this.props.autoFocus}
            floatingLabelText={this.props.floatingLabelText}
            onChange={this.handleFieldChange}
            type={this.props.type}
            defaultValue={this.state.fieldValue}
            style={this.props.style}

        />);
    }
}
