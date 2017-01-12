import * as React from "react";
import {Row, Col} from "../../views/common-components/Flexboxgrid";
import {Paper, RaisedButton} from "material-ui";
import {ReduxState} from "../../redux/ReduxState";
import {connect} from "react-redux";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import {ChallengeDTO} from "../ChallengeDTO";
import {acceptOrRejectChallenge} from "../challengeActions";
import {customChallengeStatusSelector} from "../challengeSelectors";


interface Props {
    challenge: ChallengeDTO
}
interface ReduxProps {
    userId: number,
    challengeState: String
}
interface ReduxFunc {
    onAcceptRejectChallenge: (challengeId: number, accept: boolean) => void
}

class ChallengeAcceptRejectMessageItemInternal extends React.Component<Props & ReduxProps & ReduxFunc, void> {

    handleConfirmChallenge = () => {
        this.props.onAcceptRejectChallenge(this.props.challenge.id, true);
    }
    handleRejectChallenge = () => {
        this.props.onAcceptRejectChallenge(this.props.challenge.id, false);
    }

    render() {

        var creatorLabel = this.props.challenge.userLabels.find(u=>u.id == this.props.challenge.creatorId).label
        var otherParticipantsThanMeAndCreator = this.props.challenge.userLabels.filter(u=>u.id != this.props.challenge.creatorId && u.id != this.props.userId);

        return <Row>

            <Col>
                <b>{creatorLabel}</b> invited you to challenge <b>{this.props.challenge.label}</b><br/>

                <Row>
                    <Col col="6">
                        <div style={{fontSize:"14px"}}>
                            Other participants:<br/>
                            <div >- {creatorLabel} (Creator)</div>
                            {
                                otherParticipantsThanMeAndCreator.map(ul=>
                                    <div key={ul.id}>- {ul.label}
                                        {this.props.challenge.creatorId == ul.id && " (Creator)" }
                                    </div>)
                            }
                        </div>
                    </Col>
                    <Col style={{padding:"10px"}}>
                        <RaisedButton
                            fullWidth={true}
                            label="Accept"
                            primary={true}
                            className="right" onClick={this.handleConfirmChallenge}/>
                    </Col>
                    <Col style={{padding:"10px"}}>
                        <RaisedButton

                            fullWidth={true}
                            label="Reject"
                            secondary={true}
                            className="right" onClick={this.handleRejectChallenge}/>
                    </Col>

                </Row>
            </Col>
            </Row>




    }
}

const mapStateToProps = (state: ReduxState, props: Props): ReduxProps => {
    return {
        userId: loggedUserSelector(state).id,
        challengeState: customChallengeStatusSelector(state, props.challenge)
    }
};
const mapDispatchToProps = (dispatch): ReduxFunc => {
    return {
        onAcceptRejectChallenge: (challengeId: number, accept: boolean) => {
            dispatch(acceptOrRejectChallenge(challengeId, accept))
        }
    }
};
let ChallengeAcceptRejectMessageItem = connect(mapStateToProps, mapDispatchToProps)(ChallengeAcceptRejectMessageItemInternal);
export default ChallengeAcceptRejectMessageItem;