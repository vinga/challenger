import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {AccountDTO} from "../AccountDTO";
import {SecondUserAuthorizePopoverDialog} from "./SecondUserAuthorizePopoverDialog";
import {loginUserAction} from "../accountActions";
import ComponentDecorator = ReactRedux.ComponentDecorator;

interface PropsFunc {
    showAuthorizeFuncIfNeeded: (eventTarget: EventTarget, userId: number)=>JQueryPromise<boolean>
}
interface Props {
    challengeAccounts: Array<AccountDTO>

}
interface ReduxProps {
    anchorComponentId?: string
    authorizingUser?: AccountDTO,
}
interface ReduxPropsFunc {
    doLoginFunc: (login: string, password: string, userId?: number)=>(any);

}
interface State {
    authorizingUser?: AccountDTO,
    popoverAnchorEl?: any,//React.ReactInstance,
    deferred?: JQueryDeferred<boolean>
}

//@connect(mapStateToProps, mapDispatchToProps)
class SecondUserAuthorizePopoverInternal extends React.Component<Props& ReduxProps &PropsFunc & ReduxPropsFunc,State> {
    constructor(props) {
        super(props);
        this.state = {
            authorizingUser: null
        };
    }

    // this method is referenced by refs, do not modify
    showAuthorizeFuncIfNeeded = (eventTarget: any, userId: number): JQueryPromise<boolean> => {

        this.state.deferred = null;
        if (this.props.challengeAccounts.some(a=>a.id == userId && a.jwtToken != null)) {
            var dfd = $.Deferred();
            dfd.resolve(true);
            return dfd.promise();
        } else {
            this.state.popoverAnchorEl = eventTarget;
            this.state.authorizingUser = this.props.challengeAccounts.find(a=>a.id == userId);
            var dfd = $.Deferred();
            this.state.deferred = dfd;
            this.setState(this.state);
            return dfd.promise();
        }
    }

    closeAuthorizePopup = () => {
        this.state.popoverAnchorEl = null;
        this.setState(this.state);
    }


    componentDidUpdate() {

        if (this.state.deferred != null && this.state.authorizingUser != null
            && this.props.challengeAccounts.find(a=>a.jwtToken != null && a.id == this.state.authorizingUser.id)) {


            this.state.deferred.resolve(true);
            this.state.deferred = null;
            this.setState(this.state);
        }
    }

    render() {
        return <SecondUserAuthorizePopoverDialog
            close={this.closeAuthorizePopup}
            user={this.state.authorizingUser}
            popoverAnchorEl={this.state.popoverAnchorEl}
            doLoginFunc={this.props.doLoginFunc}

        />
    }


}

const mapStateToProps = (state: ReduxState, ownProps: Props): any => {
    return {}
};


const mapDispatchToProps = (dispatch): ReduxPropsFunc => {
    return {
        doLoginFunc: (login: string, password: string, userId?: number) => {
            dispatch(loginUserAction(login, password, false, userId));
        }
    }
};
export const SecondUserAuthorizePopover = connect(mapStateToProps, mapDispatchToProps, null, {withRef: true})(SecondUserAuthorizePopoverInternal);

