import * as React from "react";
import TextField from "material-ui/TextField";
import CSSProperties = __React.CSSProperties;


interface Props {
    floatingLabelText?: string,
    type?: string,
    fieldValue?: string,
    style?: CSSProperties,
    autoFocus?: boolean,
    onChange?: (event: any)=>void,
    errorText?: string
    validateOnFocusOut?: boolean,
    validateOnChange?: boolean,
    validator?: (str: string)=>string,
    useRequiredValidator?: boolean
    minLengthNumber?: number,
    maxLengthNumber?: number,
    regexPattern?: string,
    checkEmailPattern?: boolean,
    name?: string,
    onEnterKeyDown?: ()=>void;
}
interface State {
    fieldValue: string,
    errorText?: string
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}


export default class TextFieldExt extends React.Component<Props,State> {

    state: State = {fieldValue: this.props.fieldValue};
    public static defaultProps: Props = {
        type: "text",
        fieldValue: "",
        style: {},
        useRequiredValidator: false,

    };

    clear = () => {
        this.state.fieldValue = "";
        this.setState(this.state);
    }

    handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            if (this.props.onEnterKeyDown != null)
                this.props.onEnterKeyDown();
        }
    }

    handleFieldChange = (event) => {

        this.state.fieldValue = event.target.value;
        this.setState(this.state);

        if (this.props.validateOnChange)
            this.checkIsValid();

        if (this.props.onChange != null)
            this.props.onChange(event);
    };
    onBlur = () => {
        this.checkIsValid();
    }

    getFieldLabel = () => {
        return this.props.floatingLabelText != null ? this.props.floatingLabelText : "Field";
    }
    checkIsValid = (): boolean => {
        this.state.errorText = null;
        if (this.props.validateOnFocusOut == null || this.props.validateOnFocusOut == true) {
            if (this.props.useRequiredValidator) {
                if (this.state.fieldValue == "")
                    this.state.errorText = this.getFieldLabel() + " is required";
            }
            if (this.state.errorText != null) {
                this.setState(this.state);
                return false;
            }
            if (this.props.minLengthNumber != null && this.props.minLengthNumber > this.state.fieldValue.length) {
                this.state.errorText = this.getFieldLabel() + " should have minimum " + this.props.minLengthNumber + " characters";
            }
            if (this.props.maxLengthNumber != null && this.props.maxLengthNumber < this.state.fieldValue.length) {
                this.state.errorText = this.getFieldLabel() + " should have maximum " + this.props.maxLengthNumber + " characters";
            }
            if (this.state.errorText != null) {
                this.setState(this.state);
                return false;
            }
            if (this.props.regexPattern != null) {
                if (!new RegExp(this.props.regexPattern).test(this.state.fieldValue))
                    this.state.errorText = this.getFieldLabel() + " is invalid";
            }
            if (this.state.errorText != null) {
                this.setState(this.state);
                return false;
            }
            if (this.props.checkEmailPattern != null) {
                if (!validateEmail(this.state.fieldValue))
                    this.state.errorText = "This is not a valid email address";
            }
            if (this.state.errorText != null) {
                this.setState(this.state);
                return false;
            }
            if (this.props.validator != null) {
                var error = this.props.validator(this.state.fieldValue);
                this.state.errorText = error;
            }
            this.setState(this.state);
        }
        return this.state.errorText == null;
    }

    render() {
        return (<TextField
            autoFocus={this.props.autoFocus}
            floatingLabelText={this.props.floatingLabelText}
            onChange={this.handleFieldChange}
            type={this.props.type}
            value={this.state.fieldValue}
            style={this.props.style}
            errorText={this.state.errorText}
            onBlur={this.onBlur}
            name={this.props.name}
            onKeyDown={this.handleKeyDown}

        />);
    }
}
