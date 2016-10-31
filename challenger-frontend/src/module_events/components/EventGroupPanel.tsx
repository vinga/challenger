import * as React from "react";
import Paper from "material-ui/Paper";
import TextFieldExt from "../../views/common-components/TextFieldExt";
import {FlatButton, FontIcon} from "material-ui";
import {copy, ReduxState} from "../../redux/ReduxState";
import {eventsSelector} from "../eventSelectors";
import {EXPAND_EVENTS_WINDOW, SHOW_TASK_EVENTS} from "../eventActionTypes";
import {connect} from "react-redux";
import {sendEvent} from "../eventActions";
import {EventType} from "../EventDTO";
import DifficultyIconButton from "../../module_tasks/components/taskTable/DifficultyIconButton";
import {TaskDTO} from "../../module_tasks/TaskDTO";
import Chip from "material-ui/Chip";
import {getColorSuperlightenForUser} from "../../views/common-components/Colors";


interface Props {
    authorId: number
}
interface ReduxProps {
    displayedEvents: Array<DisplayedEventUI>,
    eventWindowVisible: boolean,
    expandedEventWindow: boolean,
    task?: TaskDTO,
    no?: number

}
interface PropsFunc {
    onPostEventFunc: (authorId: number, post: string) => void
    onExpandFunc: () => void
    onCompressFunc: () => void
    onTaskCloseFunc: () => void
}
export interface DisplayedEventUI {
    id: number,
    authorId: number,
    authorOrdinal: number,
    authorLabel: string,
    postContent: string,
    eventType: string
}




const mapStateToProps = (state: ReduxState, ownProps: Props): ReduxProps => {
    return {
        displayedEvents: eventsSelector(state),
        eventWindowVisible: state.eventsState.eventWindowVisible,
        expandedEventWindow: state.eventsState.expandedEventWindow,
        task: state.eventsState.selectedTask,
        no: state.eventsState.selectedNo
    }
};


const mapDispatchToProps = (dispatch, ownProps: Props): PropsFunc => {
    return {
        onPostEventFunc: (authorId: number, content: string) => {
            dispatch(sendEvent(authorId, content))
        },
        onCompressFunc: () => {
            dispatch(EXPAND_EVENTS_WINDOW.new({expanded: false}))
        },
        onExpandFunc: () => {
            dispatch(EXPAND_EVENTS_WINDOW.new({expanded: true}))
        },
        onTaskCloseFunc: () => {
            dispatch(SHOW_TASK_EVENTS.new({task: null, no: null}))
        }
    }
};


class EventGroupPanelInternal extends React.Component<Props & ReduxProps & PropsFunc, { justClicked: boolean}> {
    private postField: TextFieldExt;
    private shouldScrollToBottom: boolean;

    constructor(props) {
        super(props);
        this.state = {
            justClicked: false
        }
        this.shouldScrollToBottom = null;
    }

    onPostSubmit = () => {
        var postText = this.postField.state.fieldValue;
        if (postText.length > 0) {
            this.state.justClicked = true;
            this.props.onPostEventFunc(this.props.authorId, postText);
            this.postField.clear();

        }
    }

    componentDidMount() {
        // first time scroll to bottom without animation
        if (this.shouldScrollToBottom == null) {
            var elem: any = $("#eventGroupChatContent")
            elem.scrollTop(elem.prop("scrollHeight"));
            this.shouldScrollToBottom = true;
        }
    }

    componentWillUpdate() {
        var elem = $("#eventGroupChatContent")
        var isScrolledToBottom = elem[0].scrollHeight - elem.scrollTop() - elem.outerHeight() < 1;
        this.shouldScrollToBottom = isScrolledToBottom;
    }

    componentDidUpdate() {
        var elem: any = $("#eventGroupChatContent")
        var diff = elem[0].scrollHeight - elem.scrollTop() - elem.outerHeight();
        if (this.shouldScrollToBottom) {
            if (this.state.justClicked) {
                // we want animation for small height differences only (like 1 post)
                elem.animate({scrollTop: elem.prop("scrollHeight")}, 1000);
                this.state.justClicked = false;
            } else
                elem.scrollTop(elem.prop("scrollHeight"));
        }
    }

    renderPost = (ev: DisplayedEventUI) => {
        if (ev.eventType == EventType.POST)
            return <span><span style={{marginRight:"5px"}}>{ev.authorLabel}:</span> {ev.postContent}</span>;
        else return <span><i> {ev.postContent}</i></span>;
    }

    renderTaskName = () => {
        if (this.props.task != null)
            return this.props.task.label
        else
            return  "";
    }

    render() {
        if (!this.props.eventWindowVisible)
            return null;
        //elem.scrollTop = elem.scrollHeight;

        var st = {width: "550px", height: "500px", position: "fixed", bottom: 0, right: 0, padding: "15px"}
        if (!this.props.expandedEventWindow) {
            st = copy(st).and({height: "188px"})
        }

        return <Paper style={st}>
            <div style={{display: "block", clear: "both"}}>
                <div style={{position:"absolute",left:"4px",top:"4px", verticalAlign:"center", display:"block"}}>
                    <Chip className="clickableChip" style={{backgroundColor: getColorSuperlightenForUser(this.props.no), flexBasis: 'min-content', minWidth: '40px'}}>
                        <div style={{display:"block"}}>
                           {/* {this.props.task != null &&
                            <span style={{display:"inline-block"}}>
                                    <DifficultyIconButton
                                        no={this.props.no}
                                        task={this.props.task}
                                        showTooltip={false}
                                    />
                                </span>
                            }
                           */}
                            {this.renderTaskName()}

                            {this.props.task != null &&
                            <span style={{float: "right", marginLeft: "10px"}}>
                                 <FontIcon className="fa fa-close" style={{ cursor: "pointer", marginRight:'2px', fontSize: "12px"}} onClick={this.props.onTaskCloseFunc}/>
                            </span>
                            }

                        </div>

                    </Chip>
                </div>
                <div style={{position:"absolute",right:"10px",top:"10px", fontSize:'10px', height:'26px'}}>
                    {this.props.expandedEventWindow
                        ?
                        <FontIcon className="fa fa-compress" style={{ cursor: "pointer", fontSize:'15px', marginRight:'9px'}} onClick={this.props.onCompressFunc}/>
                        :
                        <FontIcon className="fa fa-expand" style={{ cursor: "pointer", fontSize:'15px', marginRight:'9px'}} onClick={this.props.onExpandFunc}/> }
                </div>
            </div>

            <div style={{display:"flex", flexDirection:"column", justifyContent: "space-between", height:"100%"}}>
                <div id="eventGroupChatContent" style={{overflowY:"auto", marginTop:'40px'}}>
                    {

                        this.props.displayedEvents.map(p =>
                            <div
                                key={p.id}>{this.renderPost(p)}
                            </div>)
                    }
                </div>
                <div style={{display:"flex", minHeight:'35px', marginTop:'5px'}}>
                    <TextFieldExt
                        name="sendPost"
                        style={{width:"100%"}}
                        ref={(c)=>{this.postField=c}}
                        onEnterKeyDown={this.onPostSubmit}
                    />
                    <FlatButton
                        primary={true} label="Post"
                        onClick={this.onPostSubmit}
                    />
                </div>
            </div>
        </Paper>
    }
}


export const EventGroupPanel = connect(mapStateToProps, mapDispatchToProps)(EventGroupPanelInternal);