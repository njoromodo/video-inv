/**
 * JavaScript Functions for Checking Out Items, mostly showing and hiding various page elements.
 *
 * Created by tpaulus on 2/15/16.
 */
var items = [];
var owner_id = Cookies.getJSON("current_user").pubID;
var supervisor_id = 0;
var supervisor = null;

const notifyChime = new Audio("error.mp3");

document.onkeypress = function () {
    const inputBox = document.getElementById("input-itemID");

    if (document.activeElement.tagName != "INPUT" && document.activeElement.tagName != "TEXTAREA") {
        var value = inputBox.value;
        inputBox.select();
        inputBox.value = value;
    }
};

if (owner_id == null) {
    window.top.location = "403.html";
}

/**
 * Remove item from Items Array and Hide it from the Display List (removing it is hard!)
 *
 * @param rowID Row ID to remove from the list
 */
function removeItem(rowID) {
    // Hide from List
    const row = document.getElementById(rowID);
    row.parentNode.removeChild(row);

    // Remove from items Array
    var itemID = rowID.replace("i-", "");
    var index = items.indexOf(parseInt(itemID)); // Row ID are 'i-' + the item id
    if (index > -1) {
        items.splice(index, 1);
    }

}

/**
 * Add item to items array and add it to the list of items displayed
 *
 */
function addItem() {
    var inputBox = $('#input-itemID');
    var itemID = inputBox.val();

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

    inputBox.val('');
}

/**
 * Actually add the item. This function get called by the HTML Response listener in addItem().
 *
 * @param item JSON Object returned by the HTML Request (Contains ID, Name, and Comments)
 */
function doAddItem(item) {
    const itemsTable = document.getElementById("items");
    if (item == null || item.length == 0) {
        $("#error").text("Invalid Item ID!");
        document.getElementById("error").style.visibility = "visible";
        const itemID = $("#input-itemID");
        itemID.value = "";
        itemID.select();

        notifyChime.play();
    } else {
        for (var i = 0; i < item.length; i++) {
            var obj = item[i];
            if (items.indexOf(obj.id) == -1 && !obj.checked_out) {
                document.getElementById("error").style.visibility = "hidden"; // Hide the Error Message

                items[items.length] = obj.id; // Add itemID of list of Items in Check Out Batch

                var row = itemsTable.insertRow(-1);

                row.id = "i-" + obj.id;

                row.insertCell(0).innerHTML = obj.name;                        //Name

                row.insertCell(1).innerHTML = '<div id="' + row.id + '-com-div" class="comments" style="display: none"></div>' +
                    '<textarea title="Comments" name="comments" class="comments" id="' + row.id + '-com-ta">' + obj.comments + '</textarea></td>';   //Comments

                var removeButton = row.insertCell(2);                         //Remove
                removeButton.innerHTML = '<i class="fa fa-trash-o" onclick="removeItem(\'' + row.id + '\');"></i>';
                removeButton.className = "delete";

                document.getElementById("complete_button").disabled = false;

            } else if (item != null && items.indexOf(parseInt(item.id)) > -1) {
                console.log("Item already in list, skipping")
            } else if (item.checked_out) {
                console.log("Item Already Checked Out.");
                $("#error").text("That item is currently Checked Out");
                document.getElementById("error").style.visibility = "visible";
                itemID.value = "";
                itemID.select();

                notifyChime.play();
            }
        }
    }
}

/**
 * Move to next step, confirm the items that are on the list.
 * Hides the add items buttons/forms.
 */
function complete_checkout() {
    $("#add-items-buttons").hide(); // Hide Add Buttons
    $(".pageSubHead").text("Please make sure all items you are taking out are included below.");

    $("#confirm-buttons").show(); // Show Conf. Buttons

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
    $("#add-items-buttons").show(); // Show Add Buttons
    $("#confirm-buttons").hide(); // Hide Conf. Buttons
    $("#super-confirm-buttons").hide(); // Hide Supervisor Buttons

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
    if (supervisor_id != 0) {
        doSupervisorLogin(supervisor);
    } else {
        $("#item-entry").hide();
        $("#confirm-buttons").hide();

        $("#supervisor_pin").show();
    }
}

/**
 * Check if the Supervisor's PIN is valid
 */
function checkSup() {
    var supPin = $("#supervisor_pin-i").val();

    if (supPin !== null && supPin !== "") {
        var json = '{"pin": ' + supPin + '}';

        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 200) {
                    supervisor = JSON.parse(xmlHttp.responseText);
                    console.log(supervisor);
                }
                doSupervisorLogin(supervisor);

            }
        };

        xmlHttp.open('POST', "api/verifyPin");
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.send(json);
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

        $("#supervisor_pin").hide();
        $("#confirm-buttons").hide();

        $("#item-entry").show();
        $("#super-confirm-buttons").show();

        document.getElementsByClassName("pageHead").item(0).innerHTML = "Supervisor Check";
        document.getElementsByClassName("pageSubHead").item(0).innerHTML = "Please ensure that all items and notable comments have been included in the list below.<br>" +
            "Clicking \"Complete Checkout\" constitutes your signature in regards to the equipment being checked out.";
    } else {
        // Credentials Invalid

        document.getElementById("badCred").style.visibility = "visible";
        $("#supervisor_pin-i").val("");
    }
}

/**
 * Finish the Checkout:
 * Post the List to the Server (Supervisor Pin, User ID, items, and comments)
 */
function finish() {
    var postJSON = "{\n";
    postJSON += ("\"ownerID\": " + parseInt(owner_id) + ",\n");
    postJSON += "\"out_components\": {";
    postJSON += ("\"supervisorID\": " + parseInt(supervisor_id) + ",\n");
    postJSON += ("\"items\": [");

    for (var i = 0; i < items.length; i++) {
        var itemID = items[i];
        var itemComments = $("#i-" + itemID + "-com-div").text();

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
    postJSON += "}\n}";

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

    Cookies.set("conf_status", statusText);
    Cookies.set("action", "out");
    Cookies.set("count", items.length);

    window.location = "../conf.html";
}