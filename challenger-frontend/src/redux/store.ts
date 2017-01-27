import {rootReducer} from "./reducers";
import thunkMiddleware from "redux-thunk";
import {compose, createStore, applyMiddleware} from "redux";
import {hotReloadIfNeeded} from "./utils/utilsjs";
import _ = require("lodash");
//import { batchedUpdatesMiddleware } from 'redux-batched-updates';
//import debounceListener from 'redux-debounce-listener'

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };


function debounceListener(wait, options): any {
    return function (cs) {
        console.log("CS ",cs);
        return function (reducer, initialState) {
            var store = cs(reducer, initialState);
            return _extends({}, store, {
                subscribe: function subscribe(listener) {
                    var debounced = _.debounce(listener, wait, options);
                    return store.subscribe(debounced);
                }
            });
        };
    };
}
//_.debounce(batchLog, 250, { 'maxWait': 1000 });

//https://github.com/tappleby/redux-batched-subscribe

function configureStore(initialState) {

    const store = createStore(rootReducer, initialState,


        applyMiddleware(
            thunkMiddleware

        //    batchedUpdatesMiddleware
        // loggerMiddleware // neat middleware that logs actions
    ),



    );
    hotReloadIfNeeded(store);

/*    const finalCreateStore = compose(
        applyMiddleware(thunkMiddleware),
        debounceListener(500, { 'leading': true })
    )(createStore)



    store = finalCreateStore(rootReducer);*/

    return store;
}




let store = configureStore({});
export default store;

console.log(store.getState());



let unsubscribe = store.subscribe(() =>
    console.log(store.getState())
);




unsubscribe();