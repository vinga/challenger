import {rootReducer} from "./reducers";
import thunkMiddleware from "redux-thunk";
import {createStore, applyMiddleware} from "redux";
import {hotReloadIfNeeded} from "./utils/utilsjs";
import ajaxWrapper from "../logic/AjaxWrapper";

ajaxWrapper.baseUrl = "http://localhost:9080/api";

//https://github.com/tappleby/redux-batched-subscribe

function configureStore(initialState) {

    const store = createStore(rootReducer, initialState, applyMiddleware(
        thunkMiddleware
        // loggerMiddleware // neat middleware that logs actions
    ));
    hotReloadIfNeeded(store, "../reducers/reducers" );


    return store;
}

let store = configureStore({});
export default store;

console.log(store.getState());

let unsubscribe = store.subscribe(() =>
    console.log(store.getState())
);


//INCREMENT_DAY.new({amount:2});


//store.dispatch(INCREMENT_DAY.new({amount:1}));
//store.dispatch(INCREMENT_DAY.new({amount:-1}));

unsubscribe();