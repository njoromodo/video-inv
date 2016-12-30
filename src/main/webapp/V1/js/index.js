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
    getUser($("#userID").val());
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
            Cookies.set("current_user", JSON.parse(xmlHttp.responseText));
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
        $("#login").hide();
        $("#options").show();

        if (user.supervisor) {
            $("#toolsB").show();
        }
    } else {
        document.getElementById("badCred").style.visibility = "visible";
        $("#userID").val("");
    }

}
/**
 * Retrieve the Daily quote from the server
 */
function loadQuote() {
    var quoteText = Cookies.get("quoteText");
    var quoteAuthor = Cookies.get("quoteAuthor");
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

                    Cookies.set("quoteText", qText, {expires: 1});
                    Cookies.set("quoteAuthor", qAuthor, {expires: 1});

                    setQuote(qText, qAuthor);
                }
            }
        };

        xmlHttp.open('GET', "api/quote");
        xmlHttp.send();
    }
    else {
        setQuote(Cookies.get("quoteText"), Cookies.get("quoteAuthor"));
    }
}
/**
 * Update and Display the Quote on the Index Page
 * @param text Quote Text
 * @param author Quote Author
 */
function setQuote(text, author) {
    $('#quote').html(text + "<br>~" + author)
}