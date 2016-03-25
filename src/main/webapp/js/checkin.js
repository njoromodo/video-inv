var items = [];
var checkedIn = [];
var owner_id = JSON.parse(getCookie("current_user")).pubID;
var supervisor_id = 0;
var transaction_id = 0;

const notifyChime = new Audio("error.mp3");

document.onkeypress = function () {
    const inputBox = document.getElementById("input-itemID");

    if (document.activeElement.tagName !== "INPUT" && document.activeElement.tagName !== "TEXTAREA") {
        var value = inputBox.value;
        inputBox.select();
        inputBox.value = value;
    }
};

if (owner_id == null) {
    window.top.location = "403.html";
}

/**
 * Add item to CheckIn Transaction
 *
 * @param itemID itemID to add, if null, fecth it from input
 */
function addItem(itemID) {
    var inputBox = document.getElementById("input-itemID");
    if (itemID == null) {
        itemID = inputBox.value;
    }

    if (itemID !== null && itemID !== "") {
        var xmlHttp = new XMLHttpRequest();

        if (items.length == 0) {
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4) {
                    var response = xmlHttp;
                    var transaction = null;
                    if (response.status == 200) {
                        transaction = JSON.parse(xmlHttp.responseText);
                        console.log(transaction);
                        doLoadTransaction(transaction);
                        for (var i = 0; i < transaction.out_components.items.length; i++) {
                            var item = transaction.out_components.items[i];
                            if (itemID.length > 6) {
                                itemID = itemID.substring(1, itemID.length - 1);
                            }
                            if (item.pubID == itemID) {
                                doAddItem(item); // Add the Item to the List once the list has been loaded
                                break;
                            }
                        }
                    }
                    else {
                        doLoadTransaction(null);
                    }

                }
            };

            xmlHttp.open('GET', "api/transaction?id=" + itemID);
            xmlHttp.send();
        }
        else {
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4) {
                    var response = xmlHttp;
                    var item = null;
                    if (response.status == 200) {
                        item = JSON.parse(xmlHttp.responseText);
                        console.log(item);
                    }
                    doAddItem(item);
                }
            };

            xmlHttp.open('GET', "api/item?id=" + itemID);
            xmlHttp.send();
        }

    }

    inputBox.value = "";
}

function doLoadTransaction(json) {
    if (json != null) {
        const itemsTable = document.getElementById("items");
        var transactionItems = json.out_components.items;
        transaction_id = json.id;

        for (var i = 0; i < transactionItems.length; i++) {
            var item = transactionItems[i];

            items[items.length] = item.id; // Add itemID of list of Items in Check Out Batch

            var row = itemsTable.insertRow(-1);

            row.id = "i-" + item.id;

            row.insertCell(0).innerHTML = item.name;                        //Name

            row.insertCell(1).innerHTML = '<div id="' + row.id + '-com-div" class="comments" style="display: none"></div>' +
                '<textarea title="Comments" name="comments" class="comments" id="' + row.id + '-com-ta">' + item.comments + '</textarea></td>';   //Comments

            var status = row.insertCell(2);                         //Status
            status.innerHTML = '<i class="fa fa-times"></i>';
            status.className = "status";

            document.getElementById("complete_button").disabled = false;
            document.getElementById("instructions").innerHTML = "Now, scan every item that you checked out to check it back in.";
        }
    } else {
        document.getElementById("error").style.visibility = "visible";
        const itemID = document.getElementById("input-itemID");
        itemID.value = "";
        itemID.select();

        notifyChime.play();
    }
}

function doAddItem(item) {
    if (items.indexOf(item.id) != -1) {
        if (checkedIn.indexOf(item.id) != -1) {
            document.getElementById("error").style.visibility = "hidden";
            console.log("Item already checked in. Skipping!")
        } else {
            checkedIn[checkedIn.length] = item.id;
            document.getElementById("error").style.visibility = "hidden";
            var row = document.getElementById("i-" + item.id);
            row.cells[2].innerHTML = '<i class="fa fa-check"></i>';
        }
    } else {
        document.getElementById("error").innerHTML = "That item is not part of this checkout!";
        document.getElementById("error").style.visibility = "visible";

        notifyChime.play();
    }
}

/**
 * Move to next step, confirm the items that are on the list.
 * Hides the add items buttons/forms.
 */
function complete_checkin() {
    if (!isEqArrays(items, checkedIn)) {// Not all items have been checked in
        if (!confirm("Not all items have been checked in!\n" +
                "Do you want to Continue?")) {
            return false;
        }
    }

    document.getElementById("add-items-buttons").style.display = "none"; // Hide Add Buttons
    document.getElementsByClassName("pageSubHead").item(0).innerHTML = "Please make sure all items you are checking in are included below.";

    document.getElementById("confirm-buttons").style.display = ""; // Show Conf. Buttons

    var textAreas = document.getElementsByTagName("textarea");

    for (var i = 0; i < textAreas.length; i++) {
        var textArea = textAreas.item(i);
        var div = document.getElementById(textArea.id.replace("ta", "div"));

        textArea.style.display = "none";
        div.style.display = "";

        div.innerHTML = textArea.value;
    }

    var deleteButtons = document.getElementsByClassName("delete");
    for (var j = 0; j < deleteButtons.length; j++) {
        var button = deleteButtons.item(j);
        button.style.display = "none";
    }

}

/**
 * Revels the add items tools. Restores the edit tools.
 */
function back_to_add() {
    document.getElementById("add-items-buttons").style.display = ""; // Show Add Buttons
    document.getElementById("confirm-buttons").style.display = "none"; // Hide Conf. Buttons
    document.getElementById("super-confirm-buttons").style.display = "none"; // Hide Supervisor Buttons

    document.getElementsByClassName("pageHead").item(0).innerHTML = "Equipment Check Out";
    document.getElementsByClassName("pageSubHead").item(0).innerHTML = "Scan Items or enter IDs below to add to checkin";

    var divs = document.getElementsByTagName("div");

    for (var i = 0; i < divs.length; i++) {
        var div = divs.item(i);
        var ta = document.getElementById(div.id.replace("div", "ta"));

        div.style.display = "none";
        ta.style.display = "";

        ta.value = div.innerHTML;
    }

    var deleteButtons = document.getElementsByClassName("delete");
    for (var j = 0; j < deleteButtons.length; j++) {
        var button = deleteButtons.item(j);
        button.style.display = "";
    }
}

/**
 * Show Supervisor Pin Entry Form
 */
function show_supervisor_login() {
    if (supervisor_id !== 0) {
        doSupervisorLogin()
    } else {
        document.getElementById("item-entry").style.display = "none";
        document.getElementById("confirm-buttons").style.display = "none";

        document.getElementById("supervisor_pin").style.display = "";
    }
}

/**
 * Check if the Supervisor's PIN is valid
 */
function checkSup() {
    const inputBox = document.getElementById("supervisor_pin-i");
    var supPin = inputBox.value;

    if (supPin !== null && supPin !== "") {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                var user = null;

                if (response.status == 200) {
                    user = JSON.parse(xmlHttp.responseText);
                    console.log(user);
                }
                doSupervisorLogin(user);

            }
        };

        xmlHttp.open('GET', "api/verifyPin?pin=" + supPin);
        xmlHttp.send();
    }
}

/**
 * Show the Supervisor Confirmation Buttons or Display Invalid Credentials.
 * Called by the HTML Request Complete listener in checkSup().
 *
 * @param user JSON Object for the User (Contains the ID of the Supervisor User.)
 */
function doSupervisorLogin(user) {
    if (user != null && user.supervisor) {
        // Login is Valid

        supervisor_id = user.pubID;

        document.getElementById("supervisor_pin").style.display = "none";
        document.getElementById("confirm-buttons").style.display = "none";

        document.getElementById("item-entry").style.display = "";
        document.getElementById("super-confirm-buttons").style.display = "";

        document.getElementsByClassName("pageHead").item(0).innerHTML = "Supervisor Check";
        document.getElementsByClassName("pageSubHead").item(0).innerHTML = "Please ensure that all items and notable comments have been included in the list below.<br>" +
            "Clicking \"Complete In\" constitutes your signature in regards to the equipment being checked in.";
    } else {
        // Credentials Invalid

        document.getElementById("badCred").style.visibility = "visible";
        document.getElementById("supervisor_pin-i").value = "";
    }
}
/**
 * Complete the Transaction and post it to the Server
 */
function finish() {
    var postJSON = "{\n";
    postJSON += ("\"ownerID\": " + parseInt(owner_id) + ",\n");
    postJSON += "\"in_components\": {";
    postJSON += ("\"supervisorID\": " + parseInt(supervisor_id) + ",\n");
    postJSON += ("\"items\": [");

    for (var i = 0; i < checkedIn.length; i++) {
        var itemID = checkedIn[i];
        var itemComments = document.getElementById("i-" + itemID + "-com-div").innerHTML;

        var itemJSON = "{\n";
        itemJSON += ("\"id\": " + itemID + ", \n");
        itemJSON += ("\"comments\": \"" + itemComments + "\"\n");
        itemJSON += "}";

        if (i != checkedIn.length - 1) {    // Include a comma on all, except the last item in the Items List
            itemJSON += ",";
        }

        postJSON += itemJSON;
    }

    postJSON += ("]\n");
    postJSON += "}\n}";

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            console.log("Status: " + xmlHttp.status);
            doFinish(xmlHttp.status);
        }
    };

    xmlHttp.open('POST', "api/checkin?id=" + transaction_id);
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(postJSON);

}

/**
 * Redirect to Conformation Page
 * @param status Status of the Post request for the Transaction
 */
function doFinish(status) {
    var statusText;
    if (status == 202) {
        statusText = "ok";
    } else {
        statusText = "error";
    }
    setCookie("conf_status", statusText, null);
    setCookie("action", "in", null);
    setCookie("count", items.length, null);

    window.location = "conf.html";
}
