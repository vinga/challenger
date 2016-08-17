
var components=window.components;
/**
 * props.onChallengeActionStateChanged(boolean complete, id)
 */

class CrossoutCheckbox extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            complete:  (this.props.complete === 'true')
        };
        this.toggleChecked = this.toggleChecked.bind(this);
    }
    //toggleChecked = () => {
    toggleChecked(){
        this.setState({
            complete: !this.state.complete
        });
        this.props.onChallengeActionStateChanged(!this.state.complete, this.props.id);
    }
    render(){
        var randLetter = String.fromCharCode(65 + Math.floor(Math.random() * 26));
        var uniqid = randLetter + Date.now();


        return (
            <span>

          <input
              id={this.uniqid}
              type="checkbox"
              checked={this.state.complete}
              readOnly={true}
              onChange={this.handleChange}
          />
                 <label htmlFor={this.uniqid} onClick={this.toggleChecked.bind(null,this)}>&nbsp;</label>


            </span>
        );
    }
}

var ChallengeActionRow = React.createClass({


    render: function() {
        var status;
        var statusLabel;
        if (this.props.actionStatus == "Failed")
            status = "Failed";
        else {
            var checked;
            if (this.props.actionStatus==='Done')
                checked="true";
            else checked="false";

            status = <CrossoutCheckbox complete={checked} onChallengeActionStateChanged={this.props.onChallengeActionStateChanged} id={this.props.actionId}/>
        }


        var icon;
        if (this.props.icon.startsWith("fa-")) {
            var cssClasses = 'fa '+this.props.icon;
            icon = <i className={cssClasses}></i>;
        } else icon=<i className="material-icons">{this.props.icon}</i>;
        return (

                <tr>
                    <td>
                        {icon}
                    </td>
                    <td>
                        {this.props.actionName}
                    </td>
                    <td>
                        {this.props.actionType}
                    </td>
                    <td>
                        {status}{statusLabel}
                    </td>
                </tr>


        );
    }
});
//{this.props.children}

export default class ChallengeActionTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            userTableDTO: this.props.userTableDTO
        }

    }
/*var ChallengeActionTable=React.createClass({
    loadCommentsFromServer: function() {
        $.ajax({
            url: this.props.url,
            dataType: 'json',
            cache: false,
            success: function(data) {
                this.setState({userTableDTO: data});
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    componentDidMount: function() {
        this.loadCommentsFromServer();
        //setInterval(this.loadCommentsFromServer, this.props.pollInterval);
    },
    getInitialState: function () {
        return {
            userTableDTO: this.props.userTableDTO
        };
    },*/
    calculateCheckedCount() {
        var doneCounter=0;
        for (var i = 0; i < this.state.userTableDTO.actionsList.length; i++) {
            if (this.state.userTableDTO.actionsList[i].actionStatus=='Done')
                doneCounter++;
        };
        return doneCounter;
    }
    onChallengeActionStateChanged(complete, id) {
        var result = components.grepOne(this.state.userTableDTO.actionsList, function(e){ return e.id == id; });
        if (complete)
            result.actionStatus='Done';
        else
            result.actionStatus='Pending';
        this.setState({
            userTableDTO: this.state.userTableDTO
        });

    }
    render() {
        var func=this.onChallengeActionStateChanged;
        //var commentNodes = this.state.userTableDTO.actionsList.map(function(action) {

           /* return (
                <ChallengeActionRow
                                    key={action.id}
                                    actionId={action.id}
                                    icon={action.icon}
                                    actionName={action.actionName}
                                    actionType={action.actionType}
                                    actionStatus={action.actionStatus}
                                    onChallengeActionStateChanged={func}

                >

                </ChallengeActionRow>
            );*/
            var commentNodes=[];
            for (var i=0; i<this.state.userTableDTO.actionsList.length; i++) {
                var action=this.state.userTableDTO.actionsList[i];
                commentNodes.push(<ChallengeActionRow
                    key={action.id}
                    actionId={action.id}
                    icon={action.icon}
                    actionName={action.actionName}
                    actionType={action.actionType}
                    actionStatus={action.actionStatus}
                    onChallengeActionStateChanged={func}

                > </ChallengeActionRow>);
            }
      //  });


        var divStyle = {
            fontSize: '30px',
            marginRight: 30,
        };
        var className="fa fa-user cyan-text";
        if (this.props.userTableDTO.no==0)
            className="fa fa-user orange-text";
        return (
            <div>
                <h5 className="center">
                    <i className={className} style={divStyle}/>{this.props.userTableDTO.userName}

                    <span style={{marginLeft:20 + 'px'}}>{this.calculateCheckedCount()}</span>/
                    <span>{this.props.userTableDTO.actionsList.length}</span>
                </h5>
            <table>
                <thead>
                <tr>
                    <th data-field="icon"></th>
                    <th data-field="actionName">Name</th>
                    <th data-field="actionType">Type</th>
                    <th data-field="actionStatus">Status</th>
                </tr>
                </thead>
                <tbody>
                    {commentNodes}

                </tbody>
            </table>
                </div>
        );
    }
}



var kamilaList = [
    {id: 1, icon: "add", actionName: "Odkurzyć", actionType: "Every Week", actionStatus: "Done"},
    {id: 2, icon: "swap_calls", actionName: "Podlać kwiatki", actionType: "Adhoc", actionStatus: "Pending"}
];
var kamilaTable = {no:0, userName:"Kamila", date:"2016-08-02", actionsList: kamilaList};



var jacekList = [
    {id: 3, icon: "fa-book", actionName: "Komplement", actionType: "Every Week", actionStatus: "Done"},
    {id: 4, icon: "fa-car", actionName: "Samochód", actionType: "Adhoc", actionStatus: "Pending"},
    {id: 5, icon: "fa-mobile", actionName: "React", actionType: "Adhoc", actionStatus: "Pending"}
];
var jacekTable = {no: 1, userName:"Jacek", date:"2016-08-02", actionsList: jacekList};


ReactDOM.render(
    <ChallengeActionTable userTableDTO={kamilaTable} url="/api/challengeActions"/>,
    document.getElementById('userFirst')
);
ReactDOM.render(
    <ChallengeActionTable userTableDTO={jacekTable} url="/api/challengeActions"/>,
    document.getElementById('userSecond')
);


