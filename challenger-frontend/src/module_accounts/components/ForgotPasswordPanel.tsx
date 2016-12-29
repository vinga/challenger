import * as React from "react";
import TextFieldExt from "../../views/common-components/TextFieldExt.tsx";
import {Col, Row, RowCol} from "../../views/common-components/Flexboxgrid";
import {RaisedButton} from "material-ui";
import {FlatButton} from "material-ui";


interface Props {
    onForgotPassword: (email: string)=>void
    onExitToLoginFunc: ()=>void
}


export class ForgotPasswordPanel extends React.Component<Props, void> {

    private newEmailField: TextFieldExt;

    handleSetResetLink = () => {
      if (this.newEmailField.checkIsValid()) {
          this.props.onForgotPassword(this.newEmailField.state.fieldValue);
      }
    }

    render() {

        return (
            <div id="main" className="container ">

                <Row horizontal="center">

                    <Col col="3" style={{marginTop:"100px"}}>
                        <RowCol horizontal="start">
                        <h5>Reset password</h5>
                            </RowCol>
                        <RowCol horizontal="start">
                            Please provide your email:
                            <TextFieldExt

                                fullWidth={true}
                                floatingLabelText="Email:"
                                checkEmailPattern={true}
                                useRequiredValidator={true}
                                validateOnChange={true}
                                onEnterKeyDown={this.handleSetResetLink}
                                type="text"
                                ref={(c)=>{this.newEmailField=c}}/>

                        </RowCol>
                        < RowCol horizontal="end">
                            <RaisedButton label="Send reset link" onClick={this.handleSetResetLink}/>
                        </RowCol>
                        <RowCol horizontal="end">

                            <FlatButton labelStyle={{textTransform:"none", color:"#888888"}} label="Back" onClick={this.props.onExitToLoginFunc}/>

                        </RowCol>
                    </Col>
                </Row >

            </div >);
    }
}

