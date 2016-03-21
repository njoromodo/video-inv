/**
 * Manage Index Page Interactions, Routing, and Dynamically loaded content
 * Created by tpaulus on 2/15/16.
 */
var userID;

function check_out() {
    window.top.location = "checkout.html";
}

function check_in() {
    window.top.location = "checkin.html";
}

function tools() {
    window.top.location = "tools/index.html";
}

function login() {
    getUser(document.getElementById("userID").value);
}

/**
 * Call the API to see if a UserID is valid
 * @param id ID from Form
 */
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
            setCookie("current_user", xmlHttp.responseText);
        }
    };

    xmlHttp.open('GET', "api/user?id=" + id);
    xmlHttp.send();
}

/**
 * Login a User
 * @param user User to Login (Retrieved from getUser)
 */
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
/**
 * Retrieve the Daily quote from the server
 */
function loadQuote() {
    var quoteText = getCookie("quoteText");
    var quoteAuthor = getCookie("quoteAuthor");
    if (quoteText == null || quoteText == "" || quoteAuthor == null || quoteAuthor == "") {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);
                var quote = null;

                if (response.status == 200) {
                    quote = JSON.parse(xmlHttp.responseText);
                    console.log(quote);
                    var qText = quote.text;
                    var qAuthor = quote.author;

                    setCookie("quoteText", qText, getMidnight());
                    setCookie("quoteAuthor", qAuthor, getMidnight());

                    setQuote(qText, qAuthor);
                }
            }
        };

        xmlHttp.open('GET', "api/quote");
        xmlHttp.send();
    }
    else {
        setQuote(getCookie("quoteText"), getCookie("quoteAuthor"));
    }
}
/**
 * Update and Display the Quote on the Index Page
 * @param text Quote Text
 * @param author Quote Author
 */
function setQuote(text, author) {
    document.getElementById("quote").innerHTML = text + "<br>~" + author;
}