import * as React from 'react';
import {connect}  from 'react-redux';

interface StateProps {
    textPros: string,
    optionalText?: string,

}
interface DispatchProps {
    onClick1: Function,

}

class TypeCompa extends React.Component<StateProps & DispatchProps , any> {
    render() {
        return (<div onClick={this.props.onClick1}>buu{this.props.textPros}</div>);
    }
}
const mapStateToProps = (state: any, ownProp? :StateProps):StateProps  => ({
    textPros: "example text",
});
const mapDispatchToProps = (dispatch: any):DispatchProps => ({
    onClick1: () => {
        console.log('dispatch');
        dispatch({ type: 'INCREMENT_DAY', amount: 1 });
    }
});
export default connect(mapStateToProps, mapDispatchToProps)(TypeCompa);