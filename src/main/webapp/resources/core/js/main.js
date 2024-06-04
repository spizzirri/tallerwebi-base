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

//que coloque un color segun el tipoRendimiento
document.addEventListener('DOMContentLoaded', function() {
    var tipoRendimientoCells = document.querySelectorAll('.tipo-rendimiento');

    tipoRendimientoCells.forEach(function(cell) {
        var tipoRendimiento = cell.textContent.trim().toLowerCase();
        switch (tipoRendimiento) {
            case 'alto':
                cell.classList.add('tipo-alto');
                break;
            case 'normal':
                cell.classList.add('tipo-normal');
                break;
            case 'bajo':
                cell.classList.add('tipo-bajo');
                break;
            case 'descanso':
                cell.classList.add('tipo-descanso');
                break;
            default:
                break;
        }
    });
});

    document.addEventListener('DOMContentLoaded', function() {
                const retoId = document.querySelector('input[name="retoId"]').value;
                const currentUrl = window.location.pathname;

                // Check if we are on the home page or the empezar-reto page
                if (currentUrl === '/spring/home') {
                    if (localStorage.getItem('retoIniciado_' + retoId)) {
                        document.getElementById('btn-empezar').style.display = 'block';
                        document.getElementById('btn-terminado').style.display = 'none';
                    }
                } else if (currentUrl === '/spring/empezar-reto') {
                    document.getElementById('btn-empezar').style.display = 'none';
                    document.getElementById('btn-terminado').style.display = 'block';
                }

                document.getElementById('btn-empezar').addEventListener('click', function() {
                    // Guardar en localStorage que el reto ha sido iniciado
                    localStorage.setItem('retoIniciado_' + retoId, true);

                    // Ocultar el botón "Empezar"
                    document.getElementById('btn-empezar').style.display = 'none';

                    // Mostrar el botón "Terminado"
                    document.getElementById('btn-terminado').style.display = 'block';
                });
            });

document.addEventListener('DOMContentLoaded', (event) => {
    const usuarioDataDiv = document.getElementById('usuario-data');
    const rachaDeRetos = usuarioDataDiv ? usuarioDataDiv.getAttribute('data-racha') : 0;

    // Actualizar el texto de la racha
    const rachaTextoDiv = document.getElementById('racha-texto');
    if (rachaTextoDiv) {
        rachaTextoDiv.textContent = `Racha Retos: ${rachaDeRetos}`;
    }
});







