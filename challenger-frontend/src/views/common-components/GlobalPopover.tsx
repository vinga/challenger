import * as React from "react";
import {Popover} from "material-ui";
import * as ReactDOM from "react-dom";


interface State {
    popoverVisible: boolean,
    anchorEl?: any,
    ri?: JSX.Element,

}
interface GlobalPopoverRef {
    globalPopover?: GlobalPopover;
}


function getCoords(elem) { // crossbrowser version
    var box = elem.getBoundingClientRect();

    var body = document.body;
    var docEl = document.documentElement;

    var scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
    var scrollLeft = window.pageXOffset || docEl.scrollLeft || body.scrollLeft;

    var clientTop = docEl.clientTop || body.clientTop || 0;
    var clientLeft = docEl.clientLeft || body.clientLeft || 0;

    var top = box.top + scrollTop - clientTop;
    var left = box.left + scrollLeft - clientLeft;

    return {top: Math.round(top), left: Math.round(left)};
}


var popover;
var disableHiding = false;
export class GlobalPopover extends React.Component<any,State> {

    constructor(props) {
        super(props);
        this.state = {
            popoverVisible: false,
            anchorEl: null,
            ri: null


        }
    }


    componentDidMount = () => {
        window.addEventListener('mousedown', this.pageClick, false);

        var $element = $(ReactDOM.findDOMNode(popover));



        $(document).mousemove(function (e) {
            //  if (!this.state.popoverVisible || !disableHiding)
            //  return;
            var $element = $(ReactDOM.findDOMNode(popover));
            if (!$element.length)
                return;

            var mX = e.pageX;
            var mY = e.pageY;

            var x = getCoords($element[0]).left;
            var xw = x + $element.width();
            var y = getCoords($element[0]).top;
            var yh = y + $element.height();

            if (x == 0)
                return;
            var margin = 50;


            if (y - mY >= margin || x - mX >= margin || mX - xw >= margin || mY - yh >= margin) {//x-mX>margin || mY-y>margin || mX-xw>margin) {// || mY-yh>margin) {

                // var state = Object.assign({}, this.state, {popoverVisible: false, lastFrom: "mousemove"});
                //this.setState(state);
                $element.hide();


            }
        }.bind(this));
    }

    shouldComponentUpdate = () => {
        return true;
    }
    componentWillUnmount = () => {
        window.removeEventListener('mouseDown', this.pageClick, false);
    }
    componentWillUpdate = () => {

    }
    componentDidUpdate = () => {

    }



    pageClick = (e) => {


    }
    closePopover = () => {
        this.state.popoverVisible = false;
        var state = Object.assign({}, this.state, {popoverVisible: false});
        this.setState(state);
    }


    showPopover = (ri: JSX.Element, anchorEl) => {
        var state = {
            popoverVisible: true,
            anchorEl: anchorEl,
            ri: ri
        }


        this.setState(state);

        var $element = $(ReactDOM.findDOMNode(popover));
        if (!$element.length)
            return;
        disableHiding = true;

        $element.show();


        // two lines below are in order of simple tooltip (class=tooltip, toooltiptext) not to be 'trimmed'
        $element.parent().parent().parent().css( "overflow", "visible" );
        $element.parent().parent().css( "overflow", "visible" );

        setTimeout(()=> {
            disableHiding = false;
        }, 100)
    }


    render() {


        return <Popover

            ref={c=>{globalPopoverReff.globalPopover=this;}}
            open={this.state.popoverVisible}
            anchorEl={this.state.anchorEl}
            anchorOrigin={{horizontal: 'right', vertical: 'top'}}
            targetOrigin={{horizontal: 'left', vertical: 'top'}}
            useLayerForClickAway={false}

        >
            <div id="testme" style={{display:'block'}} ref={c=>{popover=c;}}>
                {this.state.ri}
            </div>
        </Popover>;
    }
}
export const globalPopoverReff: GlobalPopoverRef = {globalPopover: null}