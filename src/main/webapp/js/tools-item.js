/**
 * Item Utilities Functions
 */

var user_id = JSON.parse(getCookie("current_user")).pubID;
if (user_id == null) {
    window.top.location = "../403.html";
}

var currentItemID = 0;

document.onkeypress = function () {
    /**
     * Directs all key input into the Item ID field if it is not in a Text Area
     */
    const inputBox = document.getElementById("itemID");

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
    document.getElementById("view").style.display = "none";
    document.getElementById("create").style.display = "";
}
/**
 * Show View Item Form
 */
function showView() {
    document.getElementById("view").style.display = "";
    document.getElementById("create").style.display = "none";
}
/**
 * Create new Item in DB
 */
function addItem() {
    var name = document.getElementById("itemName").value;
    var short = document.getElementById("itemShortName").value;
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
    var itemID = document.getElementById("itemID").value;
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                var item = JSON.parse(xmlHttp.responseText);
                console.log(item);

                currentItemID = item.pubID;
                doShowItem(item);
                document.getElementById("itemID").value = "";
                document.getElementById("error").style.display = "none";
            }

            else {
                document.getElementById("itemID").value = "";
                document.getElementById("error").style.display = "";
                document.getElementById("item-name").style.visibility = 'hidden';
                document.getElementById("comments").style.visibility = 'hidden';
                document.getElementById("reprintButton").style.visibility = 'hidden';

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
    var itemName = document.getElementById("item-name");
    var itemComments = document.getElementById("comments");
    var reprintButton = document.getElementById("reprintButton");

    itemName.innerHTML = json.name;
    itemComments.innerHTML = json.comments;

    itemName.style.visibility = 'visible';
    itemComments.style.visibility = 'visible';
    reprintButton.style.visibility = 'visible';

}