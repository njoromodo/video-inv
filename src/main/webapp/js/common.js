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
    for (var i = array.length; i--;) {
        if (array[i] === el) return true;
    }
    return false;
}

function isEqArrays(arr1, arr2) {
    if (arr1.length !== arr2.length) {
        return false;
    }
    for (var i = arr1.length; i--;) {
        if (!inArray(arr2, arr1[i])) {
            return false;
        }
    }
    return true;
}

function setCookie(cname, cvalue, expire_date) {
    var expires;
    if (expire_date != null && expire_date.length > 0) {
        expires = "expires=" + expire_date.toUTCString();
    } else {
        expires = "";
    }
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function deleteCookie(cname) {
    document.cookie = cname + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC";
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}

function getMidnight() {
    var d = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);

    return d;
}

function getPosition(str, m, i) {
    return str.split(m, i).join(m).length;
}