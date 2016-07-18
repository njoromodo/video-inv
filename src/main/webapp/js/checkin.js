var items = [];
var checkedIn = [];
var owner_id = Cookies.getJSON("current_user").pubID;
var supervisor_id = 0;
var supervisor = null;

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
    var inputBox = $('#input-itemID');
    if (itemID == null) {
        itemID = inputBox.val();
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

    inputBox.val('');
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
            status.innerHTML = '<span class="out"><i class="fa fa-times"></i>&nbsp; Out</span>';
            status.className = "status";

            document.getElementById("complete_button").disabled = false;
            $('#instructions').text("Now, scan every item that you checked out to check it back in.");
        }
    } else {
        document.getElementById("error").style.visibility = "visible";
        const itemID = document.getElementById("input-itemID");
        itemID.value = "";
        itemID.select();

        notifyChime.play();
    }
}

function doAddItem(addItems) {
    if (addItems == null || addItems.length == 0) {
        $("#error").text("Invalid Item ID!");
        document.getElementById("error").style.visibility = "visible";
        const itemID = document.getElementById("input-itemID");
        itemID.value = "";
        itemID.select();

        notifyChime.play();
    } else {
        for (var i = 0; i < addItems.length; i++) {
            var obj = addItems[i];

            if (items.indexOf(obj.id) != -1) {
                if (checkedIn.indexOf(obj.id) != -1) {
                    document.getElementById("error").style.visibility = "hidden";
                    console.log("Item already checked in. Skipping!")
                } else {
                    checkedIn[checkedIn.length] = obj.id;
                    document.getElementById("error").style.visibility = "hidden";
                    var row = document.getElementById("i-" + obj.id);
                    row.cells[2].innerHTML = '<span class="in"><i class="fa fa-check"></i>&nbsp; In</span>';
                }
            } else {
                document.getElementById("error").innerHTML = "That item is not part of this checkout!";
                document.getElementById("error").style.visibility = "visible";

                notifyChime.play();
            }
        }
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

   $("#add-items-buttons").hide(); // Hide Add Buttons
   $(".pageSubHead").text("Please make sure all items you are checking in are included below.");

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

    $(".pageHead").text("Equipment Check Out");
    $(".pageSubHead").text("Scan Items or enter IDs below to add to check in");

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
        doSupervisorLogin(supervisor)
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
    Cookies.set("conf_status", statusText);
    Cookies.set("action", "in");
    Cookies.set("count", items.length);

    window.location = "conf.html";
}
