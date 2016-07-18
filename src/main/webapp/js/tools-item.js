/**
 * Item Utilities Functions
 */

var user_id = Cookies.getJSON("current_user").pubID;
if (user_id == null) {
    window.top.location = "../403.html";
}

var currentItemID = 0;

document.onkeypress = function () {
    /**
     * Directs all key input into the Item ID field if it is not in a Text Area
     */
    var inputBox = $("#itemID");

    if (document.activeElement.tagName !== "INPUT" && document.activeElement.tagName !== "TEXTAREA") {
        var value = inputBox.value;
        inputBox.select();
        inputBox.value = value;
    }
};

/**
 * Show Create Item Form
 */
function showCreate() {
    $("#view").hide();
    $("#create").show();
}
/**
 * Show View Item Form
 */
function showView() {
    $("#view").show();
    $("#create").hide();
}
/**
 * Create new Item in DB
 */
function addItem() {
    var name = $("#itemName").val();
    var short = $("#itemShortName").val();
    var xmlHttp = new XMLHttpRequest();

    var json = '{' +
        '"name": "' + name + '" ,' +
        '"shortName": "' + short + '"' +
        '}';

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 201) {
                var item = JSON.parse(xmlHttp.responseText);
                getLabel(item.pubID);
            }
        }
    };

    xmlHttp.open('POST', "../api/addItem");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}
/**
 * Get Dymo Label by ID
 * @param id Public ID
 */
function getLabel(id) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                makeLabel(xmlHttp.responseText);
            }
        }
    };

    xmlHttp.open('GET', "../api/label?id=" + id);
    xmlHttp.send();
}
/**
 * Reprint a Label
 */
function reprint() {
    var id = currentItemID;
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                makeLabel(xmlHttp.responseText);
            }
        }
    };

    xmlHttp.open('GET', "../api/label?id=" + id);
    xmlHttp.send();
}
/**
 * Get an Item by ID
 */
function getItemByID() {
    var itemID = $("#itemID").val();
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            var responseJSON;

            if (response.status == 200) {
                responseJSON = JSON.parse(xmlHttp.responseText);
                console.log(responseJSON);

            }

            if (responseJSON != null && responseJSON.length == 1) {
                var item = responseJSON[0];
                currentItemID = item.pubID;
                doShowItem(item);
                $("#itemID").val('');
                $("#error").hide();
            }

            else {
                $("#itemID").val('');
                $("#error").show();
                document.getElementById("item-panel").style.visibility = 'hidden';

            }
        }
    };

    xmlHttp.open('GET', "../api/item?id=" + itemID);
    xmlHttp.send();


}
/**
 * Load and Show the Item Information based on the Item JSON
 * @param json Item JSON
 */
function doShowItem(json) {
    var itemName = $("#item-name");
    var itemShort = $("#item-short");
    var itemComments = $("#comments");

    itemName.text(json.name);
    if (json.shortName != null && json.shortName.length > 0) {
        itemShort.text("[" + json.shortName + "]");
    }
    itemComments.text(json.comments);

    document.getElementById("item-panel").style.visibility = 'visible';
}