
function convertHex(hex,opacity){
    hex = hex.replace('#','');
    var r = parseInt(hex.substring(0,2), 16);
    var g = parseInt(hex.substring(2,4), 16);
    var b = parseInt(hex.substring(4,6), 16);

    var result = 'rgba('+r+','+g+','+b+','+opacity/100+')';
    return result;
}

const colors={
    userColors: ['#ff9800','#00bcd4'],
    userColorsLighten: ["#ffcc80", "#80deea"],
    userColorsSuperlighten: ["#FFEBCE", "#CAF1F6"],
  //  userColorsDarken3: ["#ef6c00", "#00838f"],
    userColorsTextClass: ["orange-text", "cyan-text"],
    easyColors:['#FFEBCE','#CEF2F7'],


};
export default colors;

// export const getColorLightenForUser = (userId:number):String => {
//   return colors.userColorsLighten[userId % colors.userColorsLighten.length];
// };

export const getColorSuperlightenForUser = (userId:number|Number):String => {
    return colors.userColorsSuperlighten[<number>userId % colors.userColorsLighten.length];
};
export const getColorLightenForUser = (userId:number|Number, opacity: number = null):String => {
    if (opacity==null)
        return colors.userColorsLighten[<number>userId % colors.userColorsLighten.length];
    return convertHex(colors.userColorsLighten[<number>userId % colors.userColorsLighten.length], opacity);
};

