function createUser() {
    var fname = document.getElementById("user_first").value;
    var lname = document.getElementById("user_last").value;
    var sup = document.getElementById("supervisor").value;
    var pin = document.getElementById("pin").value;

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 201) {
                var user = JSON.parse(xmlHttp.responseText);
                getLabel(user.pubID);
            }
        }
    };

    xmlHttp.open('GET', "../api/addUser?first=" + fname + "&last=" + lname + "&sup=" + sup + "&pin=" + pin);
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
                document.getElementById("createForm").reset();
            }
        }
    };

    xmlHttp.open('GET', "../api/userLabel?id=" + id);
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