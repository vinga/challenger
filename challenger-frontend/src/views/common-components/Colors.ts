


const colors={
    userColors: ['#ff9800','#00bcd4'],
    userColorsLighten: ["#ffcc80", "#80deea"],
    userColorsSuperlighten: ["#FFEBCE", "#CAF1F6"],
  //  userColorsDarken3: ["#ef6c00", "#00838f"],
    userColorsTextClass: ["orange-text", "cyan-text"],
    easyColors:['#FFEBCE','#CEF2F7'],


}
export default colors;

export const getColorLightenForUser = (userId:Number):String => {
  return colors.userColorsLighten[userId % colors.userColorsLighten.length];
}

export const getColorSuperlightenForUser = (userId:Number):String => {
    return colors.userColorsSuperlighten[userId % colors.userColorsLighten.length];
}