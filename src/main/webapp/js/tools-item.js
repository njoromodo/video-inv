/**
 * Item Utilities Functions
 */

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
function showCreate(){
    document.getElementById("view").style.display  ="none";
    document.getElementById("create").style.display = "";
}
/**
 * Show View Item Form
 */
function showView() {
    document.getElementById("view").style.display  ="";
    document.getElementById("create").style.display = "none";
}
/**
 * Create new Item in DB
 */
function addItem() {
    var name = document.getElementById("itemName").value;
    var short = document.getElementById("itemShortName").value;
    var xmlHttp = new XMLHttpRequest();

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

    xmlHttp.open('GET', "../api/addItem?name=" + encodeURIComponent(name) + "&short=" + encodeURIComponent(short));
    xmlHttp.send();
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
 * Send the Label XML to the Printer via the Dymo JS Framework
 * @param xml Label XML
 */
function makeLabel(xml) {
    try {
        var label = dymo.label.framework.openLabelXml(xml);

        // select printer to print on
        // for simplicity sake just use the first LabelWriter printer
        var printers = dymo.label.framework.getPrinters();
        if (printers.length == 0)
            throw "No DYMO printers are installed. Install DYMO printers.";

        var printerName = "";
        for (var i = 0; i < printers.length; ++i) {
            var printer = printers[i];
            if (printer.printerType == "LabelWriterPrinter") {
                printerName = printer.name;
                break;
            }
        }

        if (printerName == "")
            throw "No LabelWriter printers found. Install LabelWriter printer";

        // finally print the label
        label.print(printerName);
    }
    catch (e) {
        alert(e.message || e);
    }
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