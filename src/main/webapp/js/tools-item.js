var currentItemID = 0;

function showCreate(){
    document.getElementById("view").style.display  ="none";
    document.getElementById("create").style.display = "";
}
function showView() {
    document.getElementById("view").style.display  ="";
    document.getElementById("create").style.display = "none";
}


function addItem() {
    var name = document.getElementById("itemName").value;
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

    xmlHttp.open('GET', "../api/addItem?name=" + name);
    xmlHttp.send();
}

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


function getItemByID() {
    var itemID = document.getElementById("itemName").value;
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
            }
        }
    };

    xmlHttp.open('GET', "../api/item?id=" + itemID);
    xmlHttp.send();


}

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