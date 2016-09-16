import React, {Component} from "react";
import IconButton from "material-ui/IconButton";
import ChallengeMenuNaviBar from "./ChallengeMenuNaviBar.tsx";
import {Toolbar, ToolbarGroup, ToolbarSeparator, ToolbarTitle} from "material-ui/Toolbar";
import FontIcon from "material-ui/FontIcon";


export default class Header extends React.Component {

    render() {
        return (
            <nav className="cyan lighten-1 " role="navigation">
                <div style={{position: 'absolute', left: '0px', top: '0px', fill: '#00e5ff'}}> {/*'#b2ebf2'*/}
                    <svg xmlns="http://www.w3.org/2000/svg" width="900" height="300" viewBox="0 0 900.00001 300">
                        <path
                            d="M882.785.17c1.166.434 2.328.87 3.486 1.31L898.564.173zM252.808-.294c-41.45 37.56-94.57 11.746-117.108 14.04 0 0-22.547 1.27-38.2 9.825C79.07 39.98 54.214 56.266-.033 49.308l-.22 63.267.114 42.092C8.403 137.912 98.133 53.582 131.197 52.002c7.345-.35 23.9 4.126 26.26 12.51 19.09 67.883 148.625-37.64 229.747-49.088C563.517-9.46 746.248 19.15 796.103.694z"
                            fillOpacity=".235"/>
                    </svg>
                </div>
                <Toolbar className="cyan lighten-1">
                    <ToolbarGroup firstChild={true}>
                        <img src="img/shield.svg"
                             style={{
                                 marginLeft: '30px',
                                 marginTop: '10px',
                                 height: '40px',
                                 fill: '#ffffff'
                             }}/>
                        <span style={{fontSize: '30px', marginLeft: '10px'}}>Challenger</span>
                    </ToolbarGroup>


                    {this.props.logged &&
                    <ToolbarGroup>
                        <ChallengeMenuNaviBar style={{color: 'white'}}
                                              className="right"/>
                        <ToolbarSeparator />
                        <div >
                            {
                                /*<IconButton> <FontIcon
                                 className="fa fa-user white-text"/></IconButton>
                                 <span className="loginTopInfo">{this.props.userName}</span>*/
                            }
                            <IconButton onClick={this.props.onLogout}> <FontIcon
                                className="fa fa-power-off white-text"/></IconButton>
                        </div>
                    </ToolbarGroup>
                    }
                </Toolbar>
            </nav>
        );
    }
}
