$(document).on("click", "a[href='#menu']", function(e) {
    e.preventDefault();
    e.stopPropagation();
    $("#menu").show();
});

$(document).on("click", "body", function() {
    $("#menu").hide();
});
