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