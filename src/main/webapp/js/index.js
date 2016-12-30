/**
 * Index Page Functions.
 * Manages User Status (Login/Log out), etc.
 *
 * Created by tpaulus on 4/28/16.
 */

var user = null;

var pageHistory = ['index'];

window.onload = function () {
    checkLoggedIn();
    loadView("index");
};

function checkLoggedIn() {
    var userInfo = Cookies.getJSON("user");
    var sessionToken = Cookies.get("session");
    if (sessionToken != null && sessionToken.length > 0 && userInfo != null) {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);
                console.log("Response:" + response.responseText);

                if (response.status == 200) {
                    console.log("Previous Session token is still valid, auto-logging in");
                    doStaffLogin(userInfo);
                } else {
                    console.log("Previous session token is no longer valid. Please login again.")
                }
            }
        };

        xmlHttp.open('get', "api/session/verify");
        xmlHttp.setRequestHeader("session", sessionToken);
        xmlHttp.send();
    }
}

function showHome() {
    if (user != null) showPage("welcome", "menu-home");
    else showPage("index", "menu-home");
}

function loadView(viewName) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            $('#view-container').html(xmlHttp.responseText);
        }
    };

    xmlHttp.open('get', "views/" + viewName + ".view");
    xmlHttp.send();
}

function showPage(pageName, navMenuID) {
    // // Will only work if the user is logged in.
    // if (pageName == "index" || user != null && pageName != pageHistory[pageHistory.length - 1]) {
    //     // Change the page only if page hasn't change
    //     $('#' + pageHistory[pageHistory.length - 1]).hide();
    //     $('#' + pageName).show();
    //     pageHistory[pageHistory.length] = pageName;
    //
    //     if (navMenuID != null) updateNav(navMenuID);
    // }


}

function updateNav(newActivePage) {
    $('.active').removeClass('active');
    $('#' + newActivePage).addClass('active');
}

function showStaffLoginModal() {
    if (user == null) {
        $('#userDropdown').prop("disabled", true);
        $('#staffLoginModal').modal('show');
        $('#inputUsername').focus();
    }
}

function hideStaffLoginModal() {
    $('#staffLoginModal').modal('hide');
    $('#userNotFoundAlert').hide();
}

function loginStaff() {
    $('#userNotFoundAlert').hide();
    var json = '{"username": "' + $('#inputUsername').val() + '",' +
        '"password": "' + $('#inputPassword').val() + '"' +
        '}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                var responseJSON = JSON.parse(xmlHttp.responseText);
                console.log(responseJSON);
                Cookies.set("session", xmlHttp.getResponseHeader("session"), {expires: 1});
                doStaffLogin(responseJSON);
            } else {
                doStaffLogin(null);
            }
        }
    };

    xmlHttp.open('POST', "api/login");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function doStaffLogin(userJSON) {
    user = userJSON;
    if (userJSON != null) {
        Cookies.set("user", JSON.stringify(userJSON), {expires: 1});
        $('#header-user-name').html(userJSON.firstName + " " + userJSON.lastName + '<span class="caret"></span>');
        $('#staffLoginModal').modal('hide');
        $('#userDropdown').prop("disabled", false);

        // TODO Show/Hide Admin Link based on User Perms.
        // TODO Fill Welcome Page Content

        $('#welcomePageName').text(userJSON.firstName + " " + userJSON.lastName);


        $('#link-checkout').removeClass('disabled');
        $('#link-checkin').removeClass('disabled');

        if (userJSON.admin) {
            $('#menu-admin').show();
        }

        showPage('welcome-content');
        setUpdateStaffModalContent(userJSON);
        $('#loginForm')[0].reset();
    } else {
        $('#userNotFoundAlert').show();
    }
}

function setUpdateStaffModalContent(userInfo) {
    $('#updateStaffUsername').text(userInfo.username);
    $('#updateStaffFirstName').val(userInfo.firstName);
    $('#updateStaffLastName').val(userInfo.lastName);
    $('#updateStaffPhoneNumber').val(userInfo.phone);
    $('#updateStaffEmail').val(userInfo.email);
}

function updateStaff() {
    $('#staffInfoModal').modal('hide');
    // TODO
}

function showStaffInfoModal() {
    $('#staffInfoModal').modal('show');
}

function logout() {
    user = null;
    Cookies.remove("user");
    Cookies.remove("session");
    showHome();
    pageHistory = ['index-content']; // Reset Page History for User

    $('#header-user-name').text("Login");
    $('#userDropdown').prop("disabled", true);

    $('#link-checkout').addClass('disabled');
    $('#link-checkin').addClass('disabled');

    $('#logoutAlert').show();
    window.setTimeout(function () {
        $('#logoutAlert').alert('close');
    }, 5000)
}