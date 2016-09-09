
$(function() {
    $.ajaxSetup({
        error: function(jqXHR, exception) {

            console.log("Error "+jqXHR.status+" "+jqXHR.responseText);


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

class AjaxWrapper {

    login(login, pass, okCallbackData, errorCallbackjqXHRException) {
        $.ajax({
            url: this.baseUrl + "/newToken",
            type: 'POST',
            data: {
                'login': login,
                'pass': pass
            },
        }).then(
            (data)=>okCallbackData(data),
            (jqXHR, exception) => errorCallbackjqXHRException(jqXHR, exception)
        );
    }

    loadVisibleChallenges(callbackData) {
        $.ajax({
            url: this.baseUrl + "/visibleChallenges",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }).then(data=>callbackData(data));
    }

    //not used
    loadIconFromServer(iconId, callbackData) {
        $.ajax({
            url: this.baseUrl + "/newAvatar/" + iconId,
            cache: false,
            success:
                (data)=>callbackData(data)

        });
    }

    loadTasksFromServer(challengeId, userNo, callback) {
        $.ajax({
            url: this.baseUrl+ ((userNo==0)?"/tasksForMe" : "/tasksForOther")+"/"+challengeId,
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }).then((data)=>callback(data));
    }


    updateTask(task, callback) {
        $.ajax({
            url: this.baseUrl+ "/updateTask",
            data: JSON.stringify(task),

            contentType:  "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }).then(function (updatedTask) {
            callback(updatedTask);
        });
    }

}
const ajaxWrapper=new AjaxWrapper();
export default ajaxWrapper;