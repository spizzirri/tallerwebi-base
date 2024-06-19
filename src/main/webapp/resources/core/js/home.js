//progreso home
document.addEventListener("DOMContentLoaded", function() {
                var tipoRendimiento = document.querySelector(".progreso-container span").innerText;
                var barraProgreso = document.querySelector(".barra-progreso");
                if (tipoRendimiento === "ALTO") {
                    barraProgreso.style.backgroundColor = "#2ecc71"; // Verde
                } else if (tipoRendimiento === "NORMAL") {
                    barraProgreso.style.backgroundColor = "#f1c40f"; // Amarillo
                } else if (tipoRendimiento === "BAJO") {
                    barraProgreso.style.backgroundColor = "#e74c3c"; // Rojo
                }
        });

        document.addEventListener('DOMContentLoaded', function() {
                const retoId = document.querySelector('input[name="retoId"]').value;
                const enProceso = document.querySelector('input[name="enProceso"]').value === 'true';

                if (enProceso) {
                    // Si el reto está en proceso, mostrar el botón "Terminado"
                    document.getElementById('btn-empezar').style.display = 'none';
                    document.getElementById('btn-terminado').style.display = 'block';
                } else {
                    // Si el reto no está en proceso, mostrar el botón "Empezar"
                    document.getElementById('btn-empezar').style.display = 'block';
                    document.getElementById('btn-terminado').style.display = 'none';
                }


            });

document.addEventListener('DOMContentLoaded', (event) => {
    const usuarioDataDiv = document.getElementById('usuario-data');
    const rachaDeRetos = usuarioDataDiv ? usuarioDataDiv.getAttribute('data-racha') : 0;

    // Actualizar el texto de la racha
    const rachaTextoDiv = document.getElementById('racha-texto');
    if (rachaTextoDiv) {
        rachaTextoDiv.textContent = `Racha ${rachaDeRetos}`;

        // Añadir clase para la animación del texto
        rachaTextoDiv.classList.add('increment');

        // Añadir clase para la animación del círculo
        const retoCirculoDiv = document.getElementById('reto-circulo');
        if (retoCirculoDiv) {
            retoCirculoDiv.classList.add('increment');
        }

        // Remover las clases después de la animación
        setTimeout(() => {
            rachaTextoDiv.classList.remove('increment');
            retoCirculoDiv.classList.remove('increment');
        }, 1000); // La duración de la animación debe coincidir con la del CSS
    }
});

  document.addEventListener('DOMContentLoaded', function () {
      let minutosRestantesInput = document.querySelector('input[name="minutosRestantes"]');
      if (minutosRestantesInput !== null) {
          let minutosRestantes = minutosRestantesInput.value;
          if (minutosRestantes > 0) {
              let tiempoRestanteElement = document.getElementById('tiempo-restante');
              let segundosRestantes = minutosRestantes * 60;

              function actualizarCronometro() {
                  let dias = Math.floor(segundosRestantes / (24 * 60 * 60));
                  let horas = Math.floor((segundosRestantes % (24 * 60 * 60)) / (60 * 60));
                  let minutos = Math.floor((segundosRestantes % (60 * 60)) / 60);
                  let segundos = segundosRestantes % 60;
                  tiempoRestanteElement.textContent = `${dias}d ${horas}h ${minutos}m ${segundos < 10 ? '0' + segundos : segundos}s`;
                  if (segundosRestantes > 0) {
                      segundosRestantes--;
                      setTimeout(actualizarCronometro, 1000);
                  } else {
                      tiempoRestanteElement.textContent = 'Tiempo agotado';
                  }
              }
              actualizarCronometro();
          }
      }
  });

    document.addEventListener("DOMContentLoaded", function() {
                const cambiarRetoIcon = document.querySelector(".cambiar-reto-icon");
                const currentURL = window.location.href;

                if (currentURL.includes("/home")) {
                    cambiarRetoIcon.style.display = "block";
                } else {
                    cambiarRetoIcon.style.display = "none";
                }
            });

document.getElementById("cambiarRetoForm").addEventListener("submit", function(event) {
   var cambiosDisponibles = document.querySelector('input[name="cambioReto"]').value;

       // Mostrar el número temporalmente
       var cambiosDisponiblesSpan = document.getElementById("cambiosDisponibles");
       cambiosDisponiblesSpan.textContent = cambiosDisponibles;
       cambiosDisponiblesSpan.style.display = "inline-block";

       // Evitar que el formulario se envíe inmediatamente
       event.preventDefault();

       // Programar el envío del formulario después de 3 segundos (3000 milisegundos)
       setTimeout(function() {
           // Ocultar el cartelito
           cambiosDisponiblesSpan.style.display = "none";
           // Ahora, permitir que el formulario se envíe
           document.getElementById("cambiarRetoForm").submit();
       }, 1500); // 3 segundos
    });