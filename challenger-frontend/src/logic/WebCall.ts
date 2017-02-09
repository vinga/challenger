//export const baseApiUrl = "http://localhost:9080/api";
import {WebCallData} from "../redux/ReduxState";
import {WEB_CALL_START, WEB_CALL_END, WEB_CALL_END_ERROR, INTERNAL_ERROR_WEB_RESPONSE, WEB_CALL_END_NO_INTERNET_CONNECTION} from "../redux/actions/actions";
import {WEB_STATUS_INTERNAL_ERROR, WEB_STATUS_UNAUTHORIZED} from "./domain/Common";
import {LOGOUT, UNAUTHORIZED_WEB_RESPONSE} from "../module_accounts/accountActionTypes";
import _ = require("lodash");
export const baseApiUrl = "/api";


export interface CallMeta {
    alwaysDisplayProgress?: boolean // default true
    async?: boolean //default false
    backgroundAction?: boolean //default false


}
function registerStartWebCall(meta: CallMeta, dispatch) {
    let callUid = Math.random();
    var webCallData: WebCallData = {
        callUid: callUid,
        startDate: new Date(),
        fromStart: meta == null || meta.alwaysDisplayProgress
    };
    if (meta == null || meta.async == false) {
        dispatch(WEB_CALL_START.new({webCallData}));
    }
    return callUid;
}

function anyAjaxJson(dispatch, path, payload: any = null, type: string, webTokens: string | string[] = null, meta?: CallMeta) {
    var data: any = {
        url: baseApiUrl + path,
        type: type,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
    }
    if (webTokens != null) {
        if (webTokens.constructor === Array)
            data.headers = {"Authorization": "Bearer " + (webTokens as Array<string>).join(" ")}
        else
            data.headers = {"Authorization": "Bearer " + webTokens}
    }

    if (payload != null)
        data.data = JSON.stringify(payload)


    var callUid = registerStartWebCall(meta, dispatch);
    return Promise.resolve($.ajax(data)).then(e => {
        dispatch(WEB_CALL_END.new({callUid}));
        return e;
    }).catch((reason: XMLHttpRequest) => {

        dispatch(WEB_CALL_END_ERROR.new({callUid}));
        if (reason.readyState == 0 && (meta==null || meta.backgroundAction!=true)) {
            // Network error (i.e. connection refused, access denied due to CORS, etc.)
            dispatch(WEB_CALL_END_NO_INTERNET_CONNECTION.new({}));
            throw reason;
        } else {
            // something weird is happening
        }

        if (reason.status == WEB_STATUS_INTERNAL_ERROR) {
            dispatch(LOGOUT.new({}));
            dispatch(INTERNAL_ERROR_WEB_RESPONSE.new({jwtToken: webTokens}));
        } else if (reason.status == WEB_STATUS_UNAUTHORIZED) {
            console.log("FOUND UNAUTHORIZED");
            dispatch(UNAUTHORIZED_WEB_RESPONSE.new({jwtToken: webTokens}));
        }


        /*if (reason.readyState == 4) {
         // HTTP error (can be checked by XMLHttpRequest.status and XMLHttpRequest.statusText)
         }
         else */


        throw reason;
        // throw Object.assign({}, reason,  {jwtToken: webTokens})
    });
}

//{readyState: 4, responseText: "{"timestamp":1483094850582,"status":500,"error":"I…ssage available","path":"/api/accounts/register"}", responseJSON: Object, status: 500, statusText: "Internal Server Error"}
/**
 * @param response example: {readyState: 4, responseText: "{"timestamp":1483094850582,"status":500,"error":"I…ssage available","path":"/api/accounts/register"}", responseJSON: Object, status: 500, statusText: "Internal Server Error"}
 * @returns {string} example: Internal Server Error (500) - No message available
 */
export function parseExceptionToHumanReadable(response: XMLHttpRequest, withMessage: boolean = true): string {

    if (response.status === 0)
        return "Connection refused"; //('Not connect.\n Verify Network.');
    /* else if (response.status == 401) {
     return responseText;
     } else {
     console.log("Error unexpected... " + status + " " + responseText);
     return "Unexpected problem"
     }*/
    console.log(response);
    if (withMessage) {
        var msg = null;

        try {
            msg = JSON.parse(response.responseText).message
        } catch (e) {
            if (typeof response.responseText === "string") {
                msg = response.responseText;
            }
        }

        if (msg != null)
            return response.statusText + ` (${response.status}): ${msg}`;
    }
    return response.statusText + ` (${response.status})`;
}


class BaseWebCall {
    webToken: string;


    postUnauthorized(dispatch, path: string, payload: any, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "POST", null, meta);
    }


    // unlike Json it sends simple string in POST without parenthesis
    postUnauthorizedString(dispatch, path: string, payload: string, meta?: CallMeta): Promise<any> {

        var callUid = registerStartWebCall(meta, dispatch);
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: payload
        })).then(e => {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        }).catch((reason: XMLHttpRequest) => {
            dispatch(WEB_CALL_END_ERROR.new({callUid}));
            throw Object.assign({}, reason)
        });
    }

    postUnauthorizedNoJson(dispatch, path: string, payload: any = null, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        var data: any = {
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",

        };
        if (payload != null) {
            data.data = payload;
        }
        return Promise.resolve($.ajax(data)).then(e => {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        }).catch((reason: XMLHttpRequest) => {
            dispatch(WEB_CALL_END_ERROR.new({callUid}));
            throw Object.assign({}, reason)
        });
    }

    postCustomNoJson(dispatch, path: string, payload: any, customJWT: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",
            data: payload,
            headers: {
                "Authorization": "Bearer " + customJWT
            },
        }))
            .then(e => {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: customJWT})
            });
    }


    get(dispatch, path: string, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, null, "GET", this.webToken, meta);
    }

    put(dispatch, path: string, payload: any, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "PUT", this.webToken, meta);
    }

    putAuthorizedCustom(dispatch, path: string, payload: any, jwtToken: string, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "PUT", jwtToken, meta);
    }

    post(dispatch, path: string, payload: any, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "POST", this.webToken, meta);
    }

    postAuthorizedCustom(dispatch, path: string, payload: any, jwtToken: string, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "POST", jwtToken, meta);
    }

    postMultiAuthorized(dispatch, path: string, payload: any, jwtTokens: Array<string>, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "POST", jwtTokens, meta);
    }

    delete(dispatch, path: string, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, null, "DELETE", this.webToken, meta);
    }


    deleteMultiAuthorized(dispatch, path: string, jwtTokens: Array<string>, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, null, "DELETE", jwtTokens, meta);
    }

    postWithFailureIfTrue(dispatch, path: string, payload: any, causeFailure: boolean, meta?: CallMeta): Promise<any> {
        return anyAjaxJson(dispatch, path, payload, "POST", causeFailure ? "failureTokenForTests" : this.webToken, meta);
    }


}


export const baseWebCall = new BaseWebCall();