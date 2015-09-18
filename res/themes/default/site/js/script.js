
$(function() {
    $("a[href='#menu']").on("click", function(e) {
        e.preventDefault();
        e.stopPropagation();
        $("#menu").slideDown(500);
    });
    $("body").on("click", function() {
        $("#menu").slideUp(500);
    });
    $("a[href^='#']").on("click", function(e) {
        e.preventDefault();
        var seltor = $(this).attr("href"),
            top = $(seltor).offset().top;
        $('html, body').animate({
            scrollTop: top-32
        }, 1000);
    });
});
