import React, {Component} from "react";

export default class ChallengeActionTableUserIcon extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            iconSvg: null
        }
       // this.loadIconFromServer();

    }
    loadIconFromServer = () => {
        $.ajax({
            url: this.props.ctx.baseUrl+"/newAvatar/"+this.props.iconId,
            cache: false,
            success: function (data) {
                this.setState({iconSvg: data});
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    }

    render() {
        var className = "fa fa-circle fa-stack-2x "+this.props.ctx.userColorsTextClass[this.props.userNo];

        var userIcon = (
            <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className}></i>
                            <i className="fa fa-user fa-stack-1x fa-inverse" style={{opacity: 0.9}}></i>
                        </span>



        );
        if (this.state.iconSvg!=null) {



            var color= this.props.ctx.userColors[this.props.userNo];
            color="white";

            var obj={__html: this.state.iconSvg};

            var userIcon = (
                <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className}></i>
                            <div className="fa-stack-1x" style={{color: color, marginLeft:'0px',marginTop:'7px',fill:color, stroke: color}} dangerouslySetInnerHTML={obj}/>
                        </span>);

        }

        return userIcon;
    }
}