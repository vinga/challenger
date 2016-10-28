//import * as Rx from 'rxjs/Rx'; /*   "rxjs": "^5.0.0-beta.12"*/



$(function () {
    $.ajaxSetup({
        error: function (jqXHR, exception) {

            console.log("Error " + jqXHR.status + " " + jqXHR.responseText);


            /*        if (jqXHR.status === 0) {
             alert('Not connect.\n Verify Network.');
             } else if (jqXHR.status == 404) {
             alert('Requested page not found. [404]');
             } else if (jqXHR.status == 500) {
             alert('Internal Server Error [500].');
             } else if (jqXHR.status == 401) {
             console.log("UNAUTHORIZED [401]");
             } else if (exception === 'parsererror') {
             alert('Requested JSON parse failed.');
             } else if (exception === 'timeout') {
             alert('Time out error.');
             } else if (exception === 'abort') {
             alert('Ajax request aborted.');
             } else {
             alert('Uncaught Error.\n' + jqXHR.responseText);
             }*/
        }
    });
});

