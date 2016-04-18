/**
 * JS Functions for Inventory Tools (Inventory List, Item and Transaction History)
 *
 * Created by tpaulus on 4/14/16.
 */

function backToList() {
    document.getElementById("loading").style.display = "none";
    document.getElementById("itemHist").style.display = "none";
    document.getElementById("transHist").style.display = "none";
    document.getElementById("item-list").style.display = "";
}

function loadItems() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var json = JSON.parse(response.responseText);
                console.log(json);
                doLoadItems(json);
            }
        }
    };

    xmlHttp.open('GET', "../api/reports/inventory");
    xmlHttp.send();
}

function doLoadItems(json) {
    var table = document.getElementById("items");
    for (var i = 0; i < json.length; i++) {
        var item = json[i];
        var row = table.insertRow();
        row.insertCell(0).innerHTML = item.name;
        var shortName = row.insertCell(1);
        shortName.innerHTML = item.shortName != null ? item.shortName : "";
        shortName.style.fontStyle = "italic";
        row.insertCell(2).innerHTML = item.pubID;
        row.insertCell(3).innerHTML = item.checked_out == false ? '<i class="fa fa-check"></i>&nbsp; In' : '<i class="fa fa-times"></i>&nbsp; Out';
        row.insertCell(4).innerHTML = item.lastTransactionDate;
        row.insertCell(5).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showHist(' + item.pubID + ');" id="tx-'+item.pubID+'"><i class="fa fa-history" aria-hidden="true"></i>&nbsp; History</button>';
        if (item.lastTransactionDate.toLowerCase() == "none") {
            document.getElementById("tx-"+item.pubID).disabled = true;
        }
    }

    backToList();
}

function showHist(itemID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var json = JSON.parse(response.responseText);
                console.log(json);
                doShowHist(json, itemID);
            }
        }
    };

    xmlHttp.open('GET', "../api/reports/history?id=" + itemID);
    xmlHttp.send();
}

function doShowHist(json, itemID) {
    document.getElementById("bc-itemID").innerHTML = itemID;
    document.getElementById("h-itemID").innerHTML = itemID;
    var table = document.getElementById("transList");
    for (var t = 0; t < json.length; t++) {
        var transaction = json[t];
        var row = table.insertRow();
        row.insertCell(-1).innerHTML = transaction.out_time.substring(0, getPosition(transaction.out_time, ' ', 3));

        var outComments = false;
        var checked_in = false;

        for (var outComponentItemNum = 0; outComponentItemNum < transaction.out_components.items.length; outComponentItemNum++) {
            var outComponentItem = transaction.out_components.items[outComponentItemNum];
            if (outComponentItem.pubID = itemID) {
                row.insertCell(1).innerHTML = outComponentItem.comments != null ? outComponentItem.comments : "";
                outComments = true;
                break;
            }
        }
        if (!outComments) {
            row.insertCell(1).innerHTML = "";
        }

        if (transaction.in_components != null) {
            for (var inComponentItemNum = 0; inComponentItemNum < transaction.in_components.items.length; inComponentItemNum++) {
                var inComponentItem = transaction.in_components.items[inComponentItemNum];
                if (inComponentItem.pubID = itemID) {
                    row.insertCell(2).innerHTML = inComponentItem.comments != null ? inComponentItem.comments : "";
                    checked_in = true;
                    break;
                }
            }
        }

        if (checked_in) {
            row.insertCell(3).innerHTML = '<i class="fa fa-check" aria-hidden="true"></i>';
        } else {
            row.insertCell(2).innerHTML = "";
            row.insertCell(3).innerHTML = '<i class="fa fa-times" aria-hidden="true"></i>';
        }

        row.insertCell(4).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showTransaction(' + transaction.id + ');"><i class="fa fa-file-text-o" aria-hidden="true"></i> &nbsp; View Transaction</button>';
    }

    document.getElementById("item-list").style.display = "none";
    document.getElementById("itemHist").style.display = "";
}

function showTransaction(transID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var json = JSON.parse(response.responseText);
                console.log(json);
                doShowTransaction(json, transID);
            }
        }
    };

    xmlHttp.open('GET', "../api/reports/transaction?id=" + transID);
    xmlHttp.send();
}

function doShowTransaction(json, transID) {
    document.getElementById("bc-transID").innerHTML = transID;
    document.getElementById("h-transID").innerHTML = transID;
    
    var table = document.getElementById("transaction");
    var transactionItems = json.out_components.items;

    getUser(json.ownerPubID);
    getSupervisor(json.out_components.supervisorID, json.in_components.supervisorID);
    
    for (var ti = 0; ti < transactionItems.length; ti++) {
        var transactionItem = transactionItems[ti];
        var row = table.insertRow();

        row.insertCell(0).innerHTML = transactionItem.name;
        row.insertCell(1).innerHTML = transactionItem.comments;
        row.insertCell(2).innerHTML = json.in_components.items[ti].comments;

        var checkedIn = false;
        for (var ci = 0; ci < json.in_components.items.length; ci++) {
            var cii = json.in_components.items[ci];
            if (transactionItem.pubID == cii.pubID) {
                checkedIn = true;
                break;
            }
        }

        if (checkedIn) {
            row.insertCell(3).innerHTML = '<i class="fa fa-check" aria-hidden="true"></i>';
        } else {
            row.insertCell(3).innerHTML = '<i class="fa fa-times" aria-hidden="true"></i>';
        }
    }

    document.getElementById("itemHist").style.display = "none";
    document.getElementById("transHist").style.display = "";
}

function getUser(userID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            var user = null;

            if (response.status == 200) {
                user = JSON.parse(xmlHttp.responseText);
                console.log(user);
                document.getElementById("h-owner").innerHTML = user.firstName + " " + user.lastName;
            }
        }
    };

    xmlHttp.open('GET', "../api/user?id=" + userID);
    xmlHttp.send();
}

function getSupervisor(outID, inID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            var user = null;

            if (response.status == 200) {
                user = JSON.parse(xmlHttp.responseText);
                console.log(user);
                document.getElementById("h-o-sup").innerHTML = user.firstName + " " + user.lastName;
            }
        }
    };

    xmlHttp.open('GET', "../api/user?id=" + outID);
    xmlHttp.send();

    var xmlHttp2 = new XMLHttpRequest();

    xmlHttp2.onreadystatechange = function () {
        if (xmlHttp2.readyState == 4) {
            var response = xmlHttp2;
            console.log("Status: " + response.status);
            var user = null;

            if (response.status == 200) {
                user = JSON.parse(xmlHttp2.responseText);
                console.log(user);
                document.getElementById("h-i-sup").innerHTML = user.firstName + " " + user.lastName;
            }
        }
    };

    xmlHttp2.open('GET', "../api/user?id=" + inID);
    xmlHttp2.send();
}

