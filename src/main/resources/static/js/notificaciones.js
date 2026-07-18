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

  /* ---------------------------------------------------------- */
  /*  mostrarToast                                               */
  /* ---------------------------------------------------------- */
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

  /* ---------------------------------------------------------- */
  /*  mostrarModalConfirmacion                                   */
  /* ---------------------------------------------------------- */
  function mostrarModalConfirmacion(opciones) {
    var config = opciones || {};
    var titulo = config.titulo || 'Aviso';
    var mensaje = config.mensaje || '';
    var textoSi = config.textoSi || 'SI';
    var textoNo = config.textoNo || 'NO';
    var onConfirm = config.onConfirm || function () {};
    var onCancel = config.onCancel || function () {};
    var unBoton = config.unBoton || false;

    var overlay = document.createElement('div');
    overlay.className = 'fixed inset-0 z-50 flex items-center justify-center px-margin-page';

    var botonesHtml;
    if (unBoton) {
      botonesHtml =
        '<button type="button" class="modal-btn-si w-full bg-secondary text-on-secondary font-headline-md py-3 rounded-full hover:opacity-90 transition-opacity">' + textoSi + '</button>';
    } else {
      botonesHtml =
        '<button type="button" class="modal-btn-no flex-1 border border-outline-variant text-on-surface font-headline-md py-3 rounded-full hover:bg-surface-container transition-colors">' + textoNo + '</button>' +
        '<button type="button" class="modal-btn-si flex-1 bg-secondary text-on-secondary font-headline-md py-3 rounded-full hover:opacity-90 transition-opacity">' + textoSi + '</button>';
    }

    overlay.innerHTML =
      '<div class="absolute inset-0 bg-on-surface/50 modal-overlay-bg"></div>' +
      '<div class="relative bg-surface-container-lowest rounded-xl shadow-lg max-w-sm w-full p-stack-lg flex flex-col items-center text-center gap-stack-md">' +
        '<div class="w-12 h-12 rounded-full bg-error-container flex items-center justify-center">' +
          '<span class="material-symbols-outlined text-on-error-container" style="font-variation-settings: \'FILL\' 1;">logout</span>' +
        '</div>' +
        '<div>' +
          '<h3 class="font-headline-md text-headline-md text-on-surface mb-1">' + titulo + '</h3>' +
          '<p class="font-body-md text-body-md text-on-surface-variant">' + mensaje + '</p>' +
        '</div>' +
        '<div class="flex gap-stack-sm w-full mt-2">' + botonesHtml + '</div>' +
      '</div>';

    document.body.appendChild(overlay);

    function cerrar() {
      overlay.remove();
    }

    overlay.querySelector('.modal-overlay-bg').addEventListener('click', function () {
      onCancel();
      cerrar();
    });

    var btnNo = overlay.querySelector('.modal-btn-no');
    if (btnNo) {
      btnNo.addEventListener('click', function () {
        onCancel();
        cerrar();
      });
    }

    overlay.querySelector('.modal-btn-si').addEventListener('click', function () {
      onConfirm();
      cerrar();
    });
  }

  window.MonteneroNotificaciones = {
    actualizarContador: actualizarContador,
    mostrarToast: mostrarToast,
    mostrarModalConfirmacion: mostrarModalConfirmacion
  };

  /* ---------------------------------------------------------- */
  /*  MonteneroTemporizador                                      */
  /* ---------------------------------------------------------- */
  window.MonteneroTemporizador = {
    TIEMPO_TOTAL_MS: 4 * 60 * 1000,
    TIEMPO_ADVERTENCIA_MS: 2 * 60 * 1000,
    INTERVALO_MS: 1000,
    STORAGE_KEY_INICIO: 'montenero_timer_inicio',
    STORAGE_KEY_AVISO: 'montenero_aviso_2min',
    STORAGE_KEY_RESERVA_ID: 'montenero_reserva_id',

    _intervalId: null,

    iniciar: function (reservaId) {
      var inicioGuardado = sessionStorage.getItem(this.STORAGE_KEY_INICIO);
      if (!inicioGuardado) {
        sessionStorage.setItem(this.STORAGE_KEY_INICIO, Date.now());
      }
      if (reservaId) {
        sessionStorage.setItem(this.STORAGE_KEY_RESERVA_ID, reservaId);
      }
      this._iniciarIntervalo();
    },

    _iniciarIntervalo: function () {
      if (this._intervalId) {
        clearInterval(this._intervalId);
      }

      var self = this;

      function tick() {
        var inicio = parseInt(sessionStorage.getItem(self.STORAGE_KEY_INICIO), 10);
        if (!inicio) {
          self.detener();
          return;
        }

        var transcurrido = Date.now() - inicio;
        var restante = Math.max(0, self.TIEMPO_TOTAL_MS - transcurrido);

        if (restante <= 0) {
          clearInterval(self._intervalId);
          self._intervalId = null;
          self._manejarTiempoAgotado();
          return;
        }

        if (restante <= self.TIEMPO_ADVERTENCIA_MS && !sessionStorage.getItem(self.STORAGE_KEY_AVISO)) {
          sessionStorage.setItem(self.STORAGE_KEY_AVISO, 'true');
          self._mostrarModalAdvertencia();
        }
      }

      tick();
      this._intervalId = setInterval(tick, this.INTERVALO_MS);
    },

    detener: function () {
      if (this._intervalId) {
        clearInterval(this._intervalId);
        this._intervalId = null;
      }
      sessionStorage.removeItem(this.STORAGE_KEY_INICIO);
      sessionStorage.removeItem(this.STORAGE_KEY_AVISO);
      sessionStorage.removeItem(this.STORAGE_KEY_RESERVA_ID);
    },

    _mostrarModalAdvertencia: function () {
      if (window.MonteneroNotificaciones) {
        MonteneroNotificaciones.mostrarModalConfirmacion({
          titulo: 'Aviso',
          mensaje: 'Le quedan 2 minutos para finalizar su compra.',
          textoSi: 'ACEPTAR',
          unBoton: true,
          onConfirm: function () {},
          onCancel: function () {}
        });
      }
    },

    _manejarTiempoAgotado: function () {
      var self = this;
      var reservaId = sessionStorage.getItem(this.STORAGE_KEY_RESERVA_ID);

      if (window.MonteneroNotificaciones) {
        MonteneroNotificaciones.mostrarModalConfirmacion({
          titulo: 'Aviso',
          mensaje: 'Lo sentimos, su tiempo de compra ha finalizado.',
          textoSi: 'ACEPTAR',
          unBoton: true,
          onConfirm: function () { self._finalizarPorTiempo(reservaId); },
          onCancel: function () { self._finalizarPorTiempo(reservaId); }
        });
      }
    },

    _finalizarPorTiempo: function (reservaId) {
      sessionStorage.removeItem(this.STORAGE_KEY_INICIO);
      sessionStorage.removeItem(this.STORAGE_KEY_AVISO);
      sessionStorage.removeItem(this.STORAGE_KEY_RESERVA_ID);

      if (reservaId) {
        fetch('/reservas/' + reservaId + '/cancelar')
          .catch(function () {})
          .finally(function () {
            window.location.href = '/viajes';
          });
      } else {
        window.location.href = '/viajes';
      }
    }
  };

  document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelector('[data-notificaciones-nav]')) {
      setInterval(actualizarContador, 30000);
    }
  });
})();
