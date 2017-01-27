
Date.prototype.addDays = function(days)
{
    var dat = new Date(this.valueOf());
    dat.setDate(dat.getDate() + days);
    return dat;
};
Date.prototype.simpleFormat = function()
{
    var d=this;
    return (""+d.getFullYear()+"-"+("00" + (d.getMonth() + 1)).slice(-2)) + "-" +
        ("00" + d.getDate()).slice(-2);
};

Date.prototype.simpleFormatWithMinutes = function()
{
    var d=this;
    return (""+d.getFullYear()+"-"+("00" + (d.getMonth() + 1)).slice(-2)) + "-" +
        ("00" + d.getDate()).slice(-2)  +" "+
        ("00" + d.getHours()).slice(-2) + ":" +
        ("00" + d.getMinutes()).slice(-2)
};

/*formatDate(d) {
    return ("00" + (d.getMonth() + 1)).slice(-2) + "/" +
        ("00" + d.getDate()).slice(-2) + "/" +
        d.getFullYear() + " " +
        ("00" + d.getHours()).slice(-2) + ":" +
        ("00" + d.getMinutes()).slice(-2)
}*/

Array.prototype.contains = function(obj) {
    return this.indexOf(obj) > -1;
};

export function hotReloadIfNeeded (store) {
    if (module.hot) {
        module.hot.accept("../reducers", () => {
            const nextRootReducer = require("../reducers");
            store.replaceReducer(nextRootReducer);
        });
    }
}