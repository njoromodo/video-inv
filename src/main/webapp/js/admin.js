/**
 * TODO Docs
 *
 * Created by tpaulus on 12/30/16.
 */

function showSection(sectionName) {
    loadChildView(sectionName);
    updateAdminNav(sectionName);
}

function updateAdminNav(newActiveView) {
    $('#admin-nav').find('.active').removeClass('active');
    $('.admin-menu-' + newActiveView).addClass('active');
}