"use strict";
(function (WebState) {
    WebState[WebState["NEED_REFETCH"] = 0] = "NEED_REFETCH";
    WebState[WebState["FETCHING"] = 1] = "FETCHING";
    WebState[WebState["FETCHING_VISIBLE"] = 2] = "FETCHING_VISIBLE";
    WebState[WebState["FETCHED"] = 3] = "FETCHED";
})(exports.WebState || (exports.WebState = {}));
var WebState = exports.WebState;
exports.WEB_STATUS_NOTHING_RETURNED_YET = 307; // for async calls
exports.WEB_STATUS_UNAUTHORIZED = 401;
exports.WEB_STATUS_INTERNAL_ERROR = 500;
