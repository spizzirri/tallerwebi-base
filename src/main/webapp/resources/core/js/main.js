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

//document.addEventListener('DOMContentLoaded', (event) => {
//    const form = document.getElementById('empezar-reto-form');
//    const btnEmpezar = document.getElementById('btn-empezar');
//    const btnTerminado = document.getElementById('btn-terminado');
//
//    if (!form || !btnEmpezar || !btnTerminado) {
//        console.error('One or more elements are missing:', {
//            form: form,
//            btnEmpezar: btnEmpezar,
//            btnTerminado: btnTerminado
//        });
//        return;
//    }
//
//        form.addEventListener('submit', function(event) {
//            event.preventDefault();  // Prevenir el comportamiento por defecto del formulario
//
//            const formData = new FormData(form);
//            const url = form.action;
//            const method = form.method;
//
//            fetch(url, {
//                method: method,
//                body: new URLSearchParams(formData)  // Convertir FormData a URLSearchParams
//            }).then(response => {
//                if (response.ok) {
//                    // Ocultar el botón "Empezar" y mostrar el botón "Terminado"
//                    btnEmpezar.style.display = 'none';
//                    btnTerminado.style.display = 'block';
//                } else {
//                    // Manejar el error si es necesario
//                    console.error('Error starting the challenge');
//                }
//            }).catch(error => {
//                console.error('An error occurred:', error);
//            });
//        });
//    });

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







