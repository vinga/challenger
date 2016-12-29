import {rootReducer} from "./reducers";
import thunkMiddleware from "redux-thunk";
import {createStore, applyMiddleware} from "redux";
import {hotReloadIfNeeded} from "./utils/utilsjs";


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




unsubscribe();