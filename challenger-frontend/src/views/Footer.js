import * as React from "react";

export const Footer = ()=> {
    return (<div >
            <div className="footer-container">
                <div className="row" style={{display: "none"}}>
                    <div className="col l6 s12">
                        <h5 className="white-text">Company Bio</h5>
                        <p className="grey-text text-lighten-4">We are a team of college students working on this project like it's
                            our full time job. Any amount would help support and continue development on this project and is
                            greatly appreciated.</p>

                    </div>
                    <div className="col l3 s12">
                        <h5 className="white-text">Settings</h5>
                        <ul>
                            <li><a className="white-text" href="#!">Link 1</a></li>
                            <li><a className="white-text" href="#!">Link 2</a></li>
                            <li><a className="white-text" href="#!">Link 3</a></li>
                            <li><a className="white-text" href="#!">Link 4</a></li>
                        </ul>
                    </div>
                    <div className="col l3 s12">
                        <h5 className="white-text">Connect</h5>
                        <ul>
                            <li><a className="white-text" href="#!">Link 1</a></li>
                            <li><a className="white-text" href="#!">Link 2</a></li>
                            <li><a className="white-text" href="#!">Link 3</a></li>
                            <li><a className="white-text" href="#!">Link 4</a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <div className="footer-copyright">
                <div className="footer-container">
                    <img src="img/shield.svg"
                         style={{height: '20px', fill: '#ffffff', marginRight: '10px'}}/>
                    Made by <a className="orange-text text-lighten-3" href="http://materializecss.com">Kameo & Iterion</a>
                </div>
            </div>
        </div>
    );

}
