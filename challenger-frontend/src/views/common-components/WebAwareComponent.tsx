import * as React from "react";
import {Component, ComponentClass} from "react";
import {WebCallState, WebCallDTO} from "../../logic/domain/Common";
import {RowCol} from "./Flexboxgrid";
import {CircularProgress} from "material-ui";

/* higher-order component */
export interface WebCallProps {
    webCall: WebCallDTO<any>;
}

export function WebCallAwareComponent<T>(InputTemplate: ComponentClass<T>, showProgressOnInitial:boolean=true): ComponentClass<T & WebCallProps> {
    return class extends Component<T & WebCallProps, void> {
        constructor(props) {
            super(props);
        }

        render() {
            console.log(this.props);
            var props = (this.props as WebCallProps);

            switch (props.webCall.webCallState) {
                case WebCallState.IN_PROGRESS:
                    return <RowCol horizontal="center">
                        Please wait...<br/>
                        <CircularProgress />
                    </RowCol>;
                case WebCallState.INITIAL:
                    if (!showProgressOnInitial) {
                        return <InputTemplate {...this.props}/>
                    }
                    return <RowCol horizontal="center">
                        Initial... <br/>
                        <CircularProgress />
                    </RowCol>;
                case WebCallState.RESPONSE_FAILURE:
                    return <RowCol horizontal="center">
                        <CircularProgress color="red"/>
                    </RowCol>;
                case WebCallState.RESPONSE_OK:
                    return <InputTemplate {...this.props}/>
                default:
                    throw new Error();

            }

        }

    }
}

