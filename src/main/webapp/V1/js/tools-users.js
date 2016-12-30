var user_id = Cookies.getJSON("current_user").pubID;
if (user_id == null) {
    window.top.location = "../403.html";
}

window.onload = function () {
    loadUsers();
};

function loadUsers() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var json = JSON.parse(response.responseText);
                console.log(json);
                doLoadUsers(json);
            }
        }
    };

    xmlHttp.open('GET', "../api/allUsers");
    xmlHttp.send();
}

function doLoadUsers(json) {
    var table = document.getElementById("users");
    for (var u = 0; u < json.length; u++) {
        var user = json[u];
        var row = table.insertRow();
        row.className = "user";
        var lastName = row.insertCell(0);
        lastName.innerHTML = user.lastName;
        var firstName = row.insertCell(1);
        firstName.innerHTML = user.firstName;
        var sup = row.insertCell(2);
        sup.innerHTML = user.supervisor ? '<i class="fa fa-check"></i>' : '<i class="fa fa-times"></i>';
        var pubID = row.insertCell(3);
        pubID.innerHTML = user.pubID;
        var reprint = row.insertCell(4);
        reprint.innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="getLabel(' + user.pubID + ');"><i class="fa fa-print"></i>&nbsp; Reprint</button>';
    }
}


function showCreate() {
    $("#list").hide();
    $("#create").show();

}

function showView() {
    $("#list").show();
    $("#create").hide();

    var table = document.getElementById("users");
    var tableRows = document.getElementsByClassName('user');

    for (var x = tableRows.length; x > 0; x--) {
        table.deleteRow(-1);
    }

    loadUsers();
}

/**
 * Create a User in the DB
 */
function createUser() {
    var fname = $("#user_first").val();
    var lname = $("#user_last").val();
    var sup = $("#supervisor").val();
    var pin = $("#pin").val();

    var json = '{' +
        '"firstName": ' + fname + ',' +
        '"lastName": ' + lname + ',' +
        '"supervisor": ' + sup + ',' +
        '"pin": "' + pin + '"' +
        '}';


    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 201) {
                var user = JSON.parse(xmlHttp.responseText);
                console.log(user);
                getLabel(user.pubID);
                showView();
            }
        }
    };

    xmlHttp.open('POST', "../api/addUser");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);

}
/**
 * Get a User Label
 * @param id User's Public ID
 */
function getLabel(id) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                makeLabel(xmlHttp.responseText);
                document.getElementById("createForm").reset();
            }
        }
    };

    xmlHttp.open('GET', "../api/userLabel?id=" + id);
    xmlHttp.send();
}