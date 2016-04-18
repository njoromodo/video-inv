/**
 * Tools Page Button Routes
 *
 * Created by tpaulus on 3/21/16.
 */

var user_id = JSON.parse(getCookie("current_user")).pubID;
if (user_id == null) {
    window.top.location = "../403.html";
}

function items() {
    window.top.location = "items.html";
}

function users() {
    window.top.location = "users.html";
}

function inventory() {
    window.top.location = "inventory.html";
}