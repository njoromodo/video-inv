/**
 * Manage Index Page Interactions, Routing, and Dynamically loaded content
 * Created by tpaulus on 2/15/16.
 *
 * TODO Improve Docs
 */
var userID;

function check_out() {
    window.top.location = "checkout.html?id=" + userID;
}

function check_in() {
    window.top.location = "checkin.html?id=" + userID;
}

function tools() {
    window.top.location = "tools/index.html";
}

function login() {
    getUser(document.getElementById("userID").value);
}

function doLogin(user) {
    if (user != null) {
        // Login Valid

        userID = user.pubID;
        document.getElementById("login").style.display = "none";
        document.getElementById("options").style.display = "";

        if (user.supervisor) {
            document.getElementById("toolsB").style.display = "";
        }
    } else {
        document.getElementById("badCred").style.visibility = "visible";
        document.getElementById("userID").value = "";
    }
}

function getUser(id) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            var user = null;

            if (response.status == 200) {
                user = JSON.parse(xmlHttp.responseText);
                console.log(user);
            }

            doLogin(user);
        }
    };

    xmlHttp.open('GET', "api/user?id=" + id);
    xmlHttp.send();
}

function loadQuote() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            var quote = null;

            if (response.status == 200) {
                quote = JSON.parse(xmlHttp.responseText);
                console.log(quote);
                setQuote(quote.text, quote.author);
            }
        }
    };

    xmlHttp.open('GET', "api/quote");
    xmlHttp.send();

}

function setQuote(text, author) {
    document.getElementById("quote").innerHTML = text + "<br>~" + author;
}