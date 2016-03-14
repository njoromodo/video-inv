/**
 * JavaScript Functions for Checking Out Items, mostly showing and hiding various page elements.
 *
 * Created by tpaulus on 2/15/16.
 */
var items = [];
var owner_id = getParameterByName("id");
var supervisor_id = 0;

const notifyChime = new Audio("error.mp3");

document.onkeypress = function () {
    const inputBox = document.getElementById("input-itemID");

    if (document.activeElement.tagName !== "INPUT" && document.activeElement.tagName !== "TEXTAREA") {
        var value = inputBox.value;
        inputBox.select();
        inputBox.value = value;
    }
};

/**
 * Remove item from Items Array and Hide it from the Display List (removing it is hard!)
 *
 * @param rowID Row ID to remove from the list
 */
function removeItem(rowID) {
    // Hide from List
    const row = document.getElementById(rowID);
    row.parentNode.removeChild(row);

    // Remove from items DB
    var itemID = rowID.replace("i-", "");
    var index = items.indexOf(parseInt(itemID)); // Row ID are 'i-' + the item id
    items = items.slice(index + 1, 1)
}

/**
 * Add item to items array and add it to the list of items displayed
 *
 */
function addItem() {
    const inputBox = document.getElementById("input-itemID");
    var itemID = inputBox.value;

    if (itemID !== null && itemID !== "") {
        var xmlHttp = new XMLHttpRequest();

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

    inputBox.value = "";
}

/**
 * Actually add the item. This function get called by the HTML Response listener in addItem().
 *
 * @param item JSON Object returned by the HTML Request (Contains ID, Name, and Comments)
 */
function doAddItem(item) {
    const itemsTable = document.getElementById("items");
    if (item != null && items.indexOf(item.id) == -1) {
        document.getElementById("error").style.visibility = "hidden"; // Hide the Error Message

        items[items.length] = item.id; // Add itemID of list of Items in Check Out Batch

        var row = itemsTable.insertRow(-1);

        row.id = "i-" + item.id;

        row.insertCell(0).innerHTML = item.name;                        //Name

        row.insertCell(1).innerHTML = '<div id="' + row.id + '-com-div" class="comments" style="display: none"></div>' +
            '<textarea title="Comments" name="comments" class="comments" id="' + row.id + '-com-ta">' + item.comments + '</textarea></td>';   //Comments

        const removeButton = row.insertCell(2);                         //Remove
        removeButton.innerHTML = '<i class="fa fa-trash-o"></i>';
        removeButton.className = "delete";
        removeButton.onclick = function () {
            removeItem(row.id)
        };

        document.getElementById("complete_button").disabled = false;

    } else if (item != null && items.indexOf(parseInt(item.id)) > -1) {
        console.log("Item already in list, skipping")
    } else {
        document.getElementById("error").style.visibility = "visible";
        const itemID = document.getElementById("input-itemID");
        itemID.value = "";
        itemID.select();

        notifyChime.play();
    }
}

/**
 * Move to next step, confirm the items that are on the list.
 * Hides the add items buttons/forms.
 */
function complete_checkout() {
    document.getElementById("add-items-buttons").style.display = "none"; // Hide Add Buttons
    document.getElementsByClassName("pageSubHead").item(0).innerHTML = "Please make sure all items you are taking out are included below.";

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
    document.getElementsByClassName("pageSubHead").item(0).innerHTML = "Scan Items or enter IDs below to add to checkout";

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
            "Clicking \"Complete Checkout\" constitutes your signature in regards to the equipment being checked out.";
    } else {
        // Credentials Invalid

        document.getElementById("badCred").style.visibility = "visible";
        document.getElementById("supervisor_pin-i").value = "";
    }
}

/**
 * Finish the Checkout:
 * Post the List to the Server (Supervisor Pin, User ID, items, and comments)
 */
function finish() {
    var postJSON = "{\n";
    postJSON += ("\"ownerID\": " + parseInt(owner_id) + ",\n");
    postJSON += ("\"supervisorID\": " + parseInt(supervisor_id) + ",\n");
    postJSON += ("\"items\": [");

    for (var i = 0; i < items.length; i++) {
        var itemID = items[i];
        var itemComments = document.getElementById("i-" + itemID + "-com-div").innerHTML;

        var itemJSON = "{\n";
        itemJSON += ("\"id\": " + itemID + ", \n");
        itemJSON += ("\"comments\": \"" + itemComments + "\"\n");
        itemJSON += "}";

        if (i != items.length - 1) {    // Include a comma on all, except the last item in the Items List
            itemJSON += ",";
        }

        postJSON += itemJSON;
    }

    postJSON += ("]\n");
    postJSON += "}";

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            console.log("Status: " + xmlHttp.status);
            doFinish(xmlHttp.status);
        }
    };

    xmlHttp.open('POST', "api/checkout");
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

    window.location = "conf.html?status=" + statusText + "&action=out&num=" + items.length;
}