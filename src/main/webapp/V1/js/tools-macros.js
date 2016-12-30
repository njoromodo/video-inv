/**
 * Macro Admin Page Functions
 *
 * Manage the Creation, Updating, and Printing of Macro Labels
 *
 * Created by tpaulus on 5/5/16.
 */

var editMacroID = null; // Set if a macro is being edited, null otherwise
var macros = new Map();

window.onload = function () {
    loadMacros();
    loadItems();
};

function loadMacros() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var json = JSON.parse(response.responseText);
                console.log(json);
                doLoadMacros(json);
            }
        }
    };

    xmlHttp.open('GET', "../api/macros/list");
    xmlHttp.send();
}

function doLoadMacros(json) {
    var table = document.getElementById("macros");
    for (var m = 0; m < json.length; m++) {
        var macro = json[m];
        macros.set(macro.id, macro);

        var row = table.insertRow();
        row.className += "macro";

        row.insertCell(0).innerHTML = macro.id;
        row.insertCell(1).innerHTML = macro.name;

        var itemList = "";
        for (var i = 0; i < macro.items.length; i++) {
            var macroItem = macro.items[i];
            itemList += macroItem + ", ";
        }
        itemList = itemList.slice(0, -2);
        row.insertCell(2).innerHTML = itemList;

        var editButton = row.insertCell(3);
        editButton.innerHTML = '<i class="fa fa-pencil-square-o" onclick="editMacro(' + macro.id + ');"></i>';
        editButton.className = "edit";

        var printButton = row.insertCell(4);
        printButton.innerHTML = '<i class="fa fa-print" onclick="printMacro(' + macro.id + ');"></i>';
        printButton.className = 'print';
    }
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
        row.insertCell(0).innerHTML = '<input type="checkbox" title="Include in Macro" class="macro-include" id="' + 'item-' + item.id + '">';
        row.insertCell(1).innerHTML = item.name + ((item.shortName != null && item.shortName.length > 0) ? " <i>[" + item.shortName + "]</i>" : "");
        row.insertCell(2).innerHTML = item.pubID;
    }
}

function showCreate() {
    $('#list').hide();
    $('#create').show();
}

function showView() {
    $('#list').show();
    $('#create').hide();

    var table = document.getElementById("macros");
    var tableRows = document.getElementsByClassName('macro');

    for (var x = tableRows.length; x > 0; x--) {
        table.deleteRow(-1);
    }

    macros.clear();
    loadMacros();
}

function editMacro(macroID) {
    editMacroID = macroID;
    var macro = macros.get(macroID);
    document.getElementById("macro_name").value = macro.name;
    for (var i = 0; i < macro.items.length; i++) {
        var macroItem = macro.items[i];
        document.getElementById("item-" + macroItem).checked = true;
    }

    $("#createFormAction").text("Edit");
    showCreate();
}

function createMacro() {
    var macroName = $("#macro_name");
    var json = '{' +
        '"name": "' + macroName.val() + '",' +
        '"items": [';

    var items = $(".macro-include");
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        var item_id = item.id.replace("item-", "");
        if (item.checked == true && item_id != null) {
            json += item_id + ','
        }
    }
    json = json.slice(0, -1); // Remove last comma from Item List
    json += ']';

    if (editMacroID != null) {
        json += ', "id": ' + editMacroID
    }
    json += '}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var json = JSON.parse(response.responseText);
                console.log(json);
                if (editMacroID == null) {
                    // Print only if the Macro is new. Don't print for edits.
                    printMacro(json.id);
                }
            }
            showView();
        }
    };

    xmlHttp.open('POST', "../api/macros/create");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);

    macroName.val('');
    for (var j = 0; j < items.length; j++) {
        items[j].checked = false;
    }
    $("#createFormAction").text("Create");
    editMacroID = null;
}

function printMacro(macroID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                makeLabel(response.responseText);
            }
        }
    };

    xmlHttp.open('GET', "../api/macros/getLabel?id=" + macroID);
    xmlHttp.send();

}