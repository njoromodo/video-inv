/**
 * Common JS Functions
 */
function inArray(array, el) {
    for (var i = array.length; i--;) {
        if (array[i] === el) return true;
    }
    return false;
}

function isEqArrays(arr1, arr2) {
    if (arr1.length !== arr2.length) {
        return false;
    }
    for (var i = arr1.length; i--;) {
        if (!inArray(arr2, arr1[i])) {
            return false;
        }
    }
    return true;
}

function getPosition(str, m, i) {
    return str.split(m, i).join(m).length;
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