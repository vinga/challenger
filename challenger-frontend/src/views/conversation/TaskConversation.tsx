import * as React from "react";

import {connect} from "react-redux";
import {ReduxState} from "../../redux/ReduxState";
import {UserDTO} from "../../logic/domain/UserDTO";
import {AccountDTO} from "../../logic/domain/AccountDTO";
import {PostDTO} from "../../logic/domain/PostDTO";
import {ConversationDTO} from "../../logic/domain/ConversationDTO";
import Paper from "material-ui/Paper";

interface Props {
    conversation: ConversationDTO
}

export default class TaskConversation extends React.Component<Props, void> {


    render() {
        return <Paper style={{width: "550px", height: "500px", position: "absolute", bottom: 0,right: 0, padding:"20px"}}>
            {

                this.props.conversation.posts.map(p => <div>{p.content}</div>)
            }
        </Paper>
    }
}