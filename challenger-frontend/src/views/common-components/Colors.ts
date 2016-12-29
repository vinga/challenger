function convertHex(hex, opacity) {
    hex = hex.replace('#', '');
    var r = parseInt(hex.substring(0, 2), 16);
    var g = parseInt(hex.substring(2, 4), 16);
    var b = parseInt(hex.substring(4, 6), 16);

    var result = 'rgba(' + r + ',' + g + ',' + b + ',' + opacity / 100 + ')';
    return result;
}

const colors = {
    userColors: [
        '#ff9800', //orange
        '#00bcd4', //cyan
        '#878787', //grey
        '#17D517', //green
        '#C74EFF', //violet
        '#FF5353', //red
    ],
    userColorsLighten: [
        "#ffcc80", //orange
        "#80deea", //cyan
        "#C3C3C3", //grey
        "#8CE88C", //green
        "#E3A6FF", //violet
        "#FFAAAA" //red
    ],
    userColorsSuperlighten: [
        "#FFEBCE", //orange
        "#CAF1F6", //cyan
        "#E8E8E8", //grey
        "#C5FFC5", //green
        "#F5DFFF", //violet
        "#FFDEDE"  //red
    ],


    easyColors: ['#FFEBCE', '#CEF2F7'],


};

export function getColor(userNo: number) {
    return colors.userColors[userNo % colors.userColors.length];
}

export const getColorSuperlightenForUser = (userId: number|Number): String => {
    return colors.userColorsSuperlighten[<number>userId % colors.userColorsLighten.length];
};
export const getColorLightenForUser = (userId: number|Number, opacity: number = null): String => {
    if (opacity == null)
        return colors.userColorsLighten[<number>userId % colors.userColorsLighten.length];
    return convertHex(colors.userColorsLighten[<number>userId % colors.userColorsLighten.length], opacity);
};

