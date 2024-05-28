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

document.addEventListener("DOMContentLoaded", function() {
    // Obtén el botón de empezar y el botón terminado
    const btnEmpezar = document.getElementById("btn-empezar");
    const btnTerminado = document.getElementById("btn-terminado");

    // Función para manejar el evento de clic en el botón de empezar
    btnEmpezar.addEventListener("click", function(event) {
        // Evita que el formulario se envíe automáticamente
        event.preventDefault();

        // Verifica si el valor del botón es 'false'
        if (btnEmpezar.value === 'false') {
            // Oculta el formulario de empezar y muestra el botón de terminado
            document.getElementById("form-empezar").style.display = "none";
            btnTerminado.style.display = "inline-block";
        }
    });
});





