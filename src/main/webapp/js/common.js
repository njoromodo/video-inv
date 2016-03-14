/**
 * Common JS Functions
 */


function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));

    // ex: var foo = getParameterByName('foo');
}

function inArray(array, el) {
    for ( var i = array.length; i--; ) {
        if ( array[i] === el ) return true;
    }
    return false;
}

function isEqArrays(arr1, arr2) {
    if ( arr1.length !== arr2.length ) {
        return false;
    }
    for ( var i = arr1.length; i--; ) {
        if ( !inArray( arr2, arr1[i] ) ) {
            return false;
        }
    }
    return true;
}

function setCookie(cname, cvalue) {
    var d = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);

    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}
