(function () {
  'use strict';

  function actualizarBadge(count) {
    document.querySelectorAll('[data-notificaciones-nav]').forEach(function (link) {
      var badge = link.querySelector('#notificaciones-badge, #notificaciones-badge-desktop, [data-notificaciones-badge]');
      if (count <= 0) {
        if (badge) {
          badge.remove();
        }
        return;
      }

      if (!badge) {
        badge = document.createElement('span');
        badge.setAttribute('data-notificaciones-badge', 'true');
        badge.className = link.classList.contains('relative')
          ? 'absolute top-0 right-2 min-w-[18px] h-[18px] px-1 flex items-center justify-center rounded-full bg-secondary text-on-secondary text-[10px] font-bold leading-none shadow-sm'
          : 'absolute -top-1 right-0 min-w-[18px] h-[18px] px-1 flex items-center justify-center rounded-full bg-secondary text-on-secondary text-[10px] font-bold leading-none';
        link.appendChild(badge);
      }

      badge.textContent = count > 99 ? '99+' : String(count);
    });
  }

  async function actualizarContador() {
    try {
      var response = await fetch('/notificaciones/api/no-leidas', {
        headers: { 'Accept': 'application/json' },
        credentials: 'same-origin'
      });
      if (!response.ok) {
        return;
      }
      var data = await response.json();
      if (typeof data.count === 'number') {
        actualizarBadge(data.count);
      }
    } catch (error) {
      /* Sin sesión o sin conexión: mantener el valor renderizado por el servidor */
    }
  }

  window.MonteneroNotificaciones = {
    actualizarContador: actualizarContador,
    mostrarToast: mostrarToast
  };

  function mostrarToast(opciones) {
    var config = opciones || {};
    var titulo = config.titulo || 'Operación exitosa';
    var mensaje = config.mensaje || '';
    var retrasoMs = typeof config.retrasoMs === 'number' ? config.retrasoMs : 2000;
    var duracionMs = typeof config.duracionMs === 'number' ? config.duracionMs : 5000;

    setTimeout(function () {
      var existente = document.getElementById('montenero-toast');
      if (existente) {
        existente.remove();
      }

      var toast = document.createElement('div');
      toast.id = 'montenero-toast';
      toast.className = 'fixed top-20 left-1/2 -translate-x-1/2 z-[100] w-[calc(100%-40px)] max-w-md bg-surface-container-lowest border border-outline-variant shadow-[0_8px_32px_rgba(0,0,0,0.12)] rounded-xl p-4 flex items-start gap-3 transition-all duration-300 opacity-0 translate-y-[-8px]';
      toast.innerHTML =
        '<div class="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center shrink-0">' +
          '<span class="material-symbols-outlined text-on-tertiary-container">check_circle</span>' +
        '</div>' +
        '<div class="flex-1 min-w-0">' +
          '<p class="font-body-lg text-body-lg font-semibold text-on-surface">' + titulo + '</p>' +
          (mensaje ? '<p class="font-body-md text-body-md text-on-surface-variant mt-1">' + mensaje + '</p>' : '') +
        '</div>' +
        '<button type="button" aria-label="Cerrar" class="text-on-surface-variant hover:opacity-70 transition-opacity p-1">' +
          '<span class="material-symbols-outlined text-[20px]">close</span>' +
        '</button>';

      document.body.appendChild(toast);

      requestAnimationFrame(function () {
        toast.classList.remove('opacity-0', 'translate-y-[-8px]');
        toast.classList.add('opacity-100', 'translate-y-0');
      });

      function cerrarToast() {
        toast.classList.add('opacity-0', 'translate-y-[-8px]');
        setTimeout(function () {
          toast.remove();
        }, 300);
      }

      toast.querySelector('button').addEventListener('click', cerrarToast);
      setTimeout(cerrarToast, duracionMs);

      actualizarContador();
    }, retrasoMs);
  }

  document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelector('[data-notificaciones-nav]')) {
      setInterval(actualizarContador, 30000);
    }
  });
})();
