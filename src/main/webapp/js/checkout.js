/**
 * Checkout Functions
 *
 * Created by tpaulus on 6/20/16.
 */


function addItem() {
    // TODO
    alert("Soon...");

    if (false) {
        swal({
            type: error,
            title: "Invalid ID!",
            text: "An Item or Macro was not found. Please try again.",
            timer: 2500,
            showConfirmButton: false
        });
    }

    markCategoryComplete("CAM");
    changesMade = true;
}

function loadKitList(fileName) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var kitJSON = JSON.parse(xmlHttp.responseText);
            var checklist = $('#checklist');

            for (var section in kitJSON) {
                if (!kitJSON.hasOwnProperty(section)) continue;
                console.log(section);

                checklist.append("<h4>" + section + "</h4>");

                for (var c = 0; c < kitJSON[section].length; c++) {
                    var component = kitJSON[section][c];
                    console.log(component);

                    checklist.append(
                        '<div class="checkout-component">' +
                        '<i class="fa fa-times-circle cat-' + component.Category + '" aria-hidden="true"></i>' + component.Name +
                        '</div>'
                    );
                }
            }
        }
    };

    xmlHttp.open('get', fileName, true);
    xmlHttp.send();
}

function markCategoryComplete(categoryName) {
    $('.cat-' + categoryName).removeClass("fa-times-circle").addClass("fa-check-circle");
}

function checklistComplete() {
    return $('.fa-times-circle').length == 0

}

function reset() {
    if (changesMade) {
        swal({
            title: "Are you sure?",
            text: "Any and all changes you have made will be lost forever!",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "Yes, reset it!",
            closeOnConfirm: true,
            html: false
        }, function(){
            loadView("checkout");
        });
    }
}

function submit() {
    if (!checklistComplete()) {
        swal("Oops...", "Not all items in the Shoot List have been added to your checkout!", "warning");
    }
}