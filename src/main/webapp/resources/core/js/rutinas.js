  function seleccionarObjetivo(card) {
      // Elimina la clase 'active' de todas las tarjetas objetivo
      var cards = document.querySelectorAll('.custom-objetivo-card');
      cards.forEach(function(card) {
          card.classList.remove('active');
      });

      // Agrega la clase 'active' a la tarjeta seleccionada
      card.classList.add('active');
  }

  function validarFormulario() {
      var objetivoSeleccionado = document.querySelector('.custom-objetivo-card.active');
      if (!objetivoSeleccionado) {
          mostrarAlerta('Debes seleccionar un objetivo antes de continuar.');
          return false;
      }
      document.getElementById('objetivo').value = objetivoSeleccionado.getAttribute('data-value');
      return true;
  }

  function mostrarAlerta(mensaje) {
      var alerta = document.createElement('div');
      alerta.className = 'animate-bounceIn bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative custom-alert mb-8'; // Agregado el margen inferior
      alerta.textContent = mensaje;
      alerta.style.fontWeight = '600'; // Aplicar fuente semibold

      var contenedor = document.querySelector('.custom-container');
      contenedor.insertBefore(alerta, contenedor.firstChild);

      setTimeout(function() {
          alerta.classList.add('animate-fadeOut'); // Añadir clase para la animación de desvanecimiento
          setTimeout(function() {
              alerta.remove();
          }, 500); // Remover la alerta después de la animación
      }, 3000); // Mostrar la alerta durante 3 segundos
  }
