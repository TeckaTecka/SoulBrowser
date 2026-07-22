/* KREZBO – mobilní menu + lightbox galerie */
(function () {
    'use strict';

    /* ---- Mobilní menu ---- */
    var toggle = document.querySelector('.nav-toggle');
    var nav = document.querySelector('.main-nav');
    if (toggle && nav) {
        toggle.addEventListener('click', function () {
            var open = nav.classList.toggle('open');
            toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        });
        nav.addEventListener('click', function (e) {
            if (e.target.tagName === 'A') {
                nav.classList.remove('open');
                toggle.setAttribute('aria-expanded', 'false');
            }
        });
    }

    /* ---- Lightbox ---- */
    var links = Array.prototype.slice.call(document.querySelectorAll('a.lightbox'));
    var lb = document.getElementById('lb');
    if (!links.length || !lb) { return; }

    var lbImg = lb.querySelector('.lb-img');
    var btnClose = lb.querySelector('.lb-close');
    var btnPrev = lb.querySelector('.lb-prev');
    var btnNext = lb.querySelector('.lb-next');
    var current = 0;

    function show(i) {
        current = (i + links.length) % links.length;
        lbImg.src = links[current].getAttribute('href');
        lbImg.alt = (links[current].querySelector('img') || {}).alt || '';
    }
    function open(i) {
        show(i);
        lb.classList.add('open');
        lb.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
    }
    function close() {
        lb.classList.remove('open');
        lb.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    }

    links.forEach(function (link, i) {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            open(i);
        });
    });

    btnClose.addEventListener('click', close);
    btnPrev.addEventListener('click', function () { show(current - 1); });
    btnNext.addEventListener('click', function () { show(current + 1); });
    lb.addEventListener('click', function (e) {
        if (e.target === lb) { close(); }
    });
    document.addEventListener('keydown', function (e) {
        if (!lb.classList.contains('open')) { return; }
        if (e.key === 'Escape') { close(); }
        else if (e.key === 'ArrowLeft') { show(current - 1); }
        else if (e.key === 'ArrowRight') { show(current + 1); }
    });
})();
