import React from 'react';
import * as ReactDOM from 'react-dom';
import App from './App.tsx';
import injectTapEventPlugin from "react-tap-event-plugin";
import {Footer} from './views/Footer'




// Needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
injectTapEventPlugin();

ReactDOM.render(<App />, document.getElementById('root'));


ReactDOM.render(<Footer/>,document.getElementById('footer'));

// facebook facebook-callback-appends-to-return-url fix
if (window.location.hash && window.location.hash == '#_=_') {
    window.location.hash = '';
}
