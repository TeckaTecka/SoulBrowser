/* TRANSEUROPE – mobilní menu + scroll nahoru */
(function () {
    'use strict';

    // Mobilní menu – postaví overlay z horní navigace
    var links = [
        ['Úvod', 'index.php'], ['Služby', 'sluzby.php'],
        ['O nás', 'o-nas.php'], ['Kontakty', 'kontakty.php'], ['Kariéra', 'kariera.php']
    ];
    var overlay = document.createElement('div');
    overlay.className = 'mobile-nav';
    var html = '<button class="close" aria-label="Zavřít">&times;</button>';
    links.forEach(function (l) { html += '<a href="' + l[1] + '">' + l[0] + '</a>'; });
    overlay.innerHTML = html;
    document.body.appendChild(overlay);

    function open() { overlay.classList.add('open'); }
    function close() { overlay.classList.remove('open'); }

    document.querySelectorAll('.nav-toggle, .js-nav-toggle').forEach(function (b) {
        b.addEventListener('click', open);
    });
    overlay.querySelector('.close').addEventListener('click', close);
    overlay.addEventListener('click', function (e) { if (e.target === overlay) close(); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') close(); });

    // Scroll nahoru
    var up = document.querySelector('.js-scrollup');
    if (up) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 400) up.classList.add('show'); else up.classList.remove('show');
        });
        up.addEventListener('click', function () { window.scrollTo({ top: 0, behavior: 'smooth' }); });
    }
})();
