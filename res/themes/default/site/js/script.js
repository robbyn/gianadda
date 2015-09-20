$(function() {
    $("a[href='#menu']").on("click", function(e) {
        e.preventDefault();
        e.stopPropagation();
        $("#menu").slideToggle(200);
    });
    $("body").on("click", function() {
        $("#menu").slideUp(200);
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
