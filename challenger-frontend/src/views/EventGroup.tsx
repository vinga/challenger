import * as React from "react";
import Paper from "material-ui/Paper";
import TextFieldExt from "./common-components/TextFieldExt";
import {FlatButton} from "material-ui";
import {FontIcon} from "material-ui";

interface Props {
    displayedEvents: Array<DisplayedEventUI>
    onPostEventFunc: (post:string) => void
}
export interface DisplayedEventUI {
    id: number,
    authorId: number,
    authorOrdinal: number,
    authorLabel: string,
    postContent: string
}



export class EventGroup extends React.Component<Props, void> {
    private postField:TextFieldExt;
    onPostSubmit = () => {
        var postText = this.postField.state.fieldValue;
        if (postText.length > 0) {
            this.props.onPostEventFunc(postText);
        }
    }
    render() {
        return <Paper style={{width: "550px", height: "500px", position: "fixed", bottom: 0,right: 0, padding:"20px"}}>
            <div style={{position:"absolute",right:"10px",top:"10px", fontSize:'10px'}}>
                <FontIcon className="fa fa-compress" style={{ fontSize:'15px', marginRight:'10px'}}/>
                <FontIcon className="fa fa-expand" style={{ fontSize:'15px'}}/>
            </div>
            <div style={{display:"flex", flexDirection:"column", justifyContent: "space-between", height:"100%"}}>
                <div>
                    {

                        this.props.displayedEvents.map(p => <div key={p.id}>{p.authorLabel}: {p.postContent}</div>)
                    }
                    Tu można by dać w ogóle wszystkie akcje
                </div>
                <div style={{display:"flex"}}><TextFieldExt
                    name="sendPost" style={{width:"100%"}}
                    ref={(c)=>{this.postField=c}}
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