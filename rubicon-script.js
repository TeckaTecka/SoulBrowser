// ── Navigation scroll + topbar ────────────────────────────
const nav = document.querySelector('.nav');
const topbar = document.querySelector('.topbar');

window.addEventListener('scroll', () => {
  const y = window.scrollY;
  nav.classList.toggle('scrolled', y > 60);
  if (topbar) topbar.classList.toggle('hidden', y > 36);
}, { passive: true });

// ── Mobile menu ───────────────────────────────────────────
const hamburger = document.querySelector('.nav__hamburger');
const mobileMenu = document.querySelector('.nav__mobile');

hamburger.addEventListener('click', () => {
  const open = hamburger.classList.toggle('open');
  hamburger.setAttribute('aria-expanded', open);
  mobileMenu.classList.toggle('open', open);
  document.body.style.overflow = open ? 'hidden' : '';
});

mobileMenu.querySelectorAll('a').forEach(link => {
  link.addEventListener('click', () => {
    hamburger.classList.remove('open');
    hamburger.setAttribute('aria-expanded', 'false');
    mobileMenu.classList.remove('open');
    document.body.style.overflow = '';
  });
});

// ── Dropdown (Podpora) ────────────────────────────────────
const dropdown = document.querySelector('.nav__dropdown');
const dropdownBtn = dropdown && dropdown.querySelector('.nav__dropdown-btn');
const dropdownMenu = dropdown && dropdown.querySelector('.nav__dropdown-menu');

if (dropdownBtn) {
  dropdownBtn.addEventListener('click', e => {
    e.stopPropagation();
    const open = dropdown.classList.toggle('open');
    dropdownBtn.setAttribute('aria-expanded', open);
  });

  document.addEventListener('click', e => {
    if (!dropdown.contains(e.target)) {
      dropdown.classList.remove('open');
      dropdownBtn.setAttribute('aria-expanded', 'false');
    }
  });

  dropdownMenu && dropdownMenu.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => {
      dropdown.classList.remove('open');
      dropdownBtn.setAttribute('aria-expanded', 'false');
    });
  });
}

// ── Smooth scroll for anchor links ───────────────────────
document.querySelectorAll('a[href^="#"]').forEach(link => {
  link.addEventListener('click', e => {
    const href = link.getAttribute('href');
    if (href === '#') return;
    const target = document.querySelector(href);
    if (!target) return;
    e.preventDefault();
    const offset = 80;
    const top = target.getBoundingClientRect().top + window.scrollY - offset;
    window.scrollTo({ top, behavior: 'smooth' });
  });
});

// ── Scroll reveal (IntersectionObserver) ─────────────────
const observer = new IntersectionObserver(entries => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('visible');
      observer.unobserve(entry.target);
    }
  });
}, { threshold: 0.1, rootMargin: '0px 0px -60px 0px' });

document.querySelectorAll('.reveal').forEach(el => observer.observe(el));

// ── Contact form submit ───────────────────────────────────
const form = document.querySelector('.form');
if (form) {
  form.addEventListener('submit', e => {
    e.preventDefault();
    const btn = form.querySelector('button[type="submit"]');
    btn.textContent = 'Odesláno ✓';
    btn.disabled = true;
    btn.style.background = '#2d6a4f';
    setTimeout(() => {
      form.reset();
      btn.textContent = 'Odeslat zprávu';
      btn.disabled = false;
      btn.style.background = '';
    }, 3000);
  });
}
