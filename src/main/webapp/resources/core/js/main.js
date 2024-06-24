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

//objetivo
document.addEventListener("DOMContentLoaded", function() {
        const cards = document.querySelectorAll(".custom-objetivo-card");
        const objetivoInput = document.getElementById("objetivo");

        cards.forEach(card => {
            card.addEventListener("click", function() {
                cards.forEach(c => c.classList.remove("selected"));
                this.classList.add("selected");
                objetivoInput.value = this.getAttribute("data-value");
            });
        });
    });

// Asegura que la primera sección esté visible al cargar la página
document.addEventListener("DOMContentLoaded", function() {
    document.getElementById('sectionDatosPersonales').classList.add('show');
});

// Función para mostrar/ocultar secciones y gestionar los botones de navegación
function mostrarSeccion(idSeccion) {
    var seccionActual = document.querySelector('.perfil-section.show');
    if (seccionActual) {
        seccionActual.classList.remove('show');
        seccionActual.style.display = 'none';
    }

    var seccionSiguiente = document.getElementById(idSeccion);
    if (seccionSiguiente) {
        seccionSiguiente.classList.add('show');
        seccionSiguiente.style.display = 'block';
    }
}


document.addEventListener("DOMContentLoaded", function () {
    const recomendacionElement = document.querySelector(".recomendacion-section p");
    if (!recomendacionElement) return;  // Verifica si el elemento existe

    const text = recomendacionElement.textContent;
    recomendacionElement.textContent = '';

    let index = 0;
    function type() {
        if (index < text.length) {
            recomendacionElement.textContent += text.charAt(index);
            index++;
            setTimeout(type, 50); // Ajusta el tiempo según sea necesario
        } else {
            recomendacionElement.style.borderRight = 'none';
        }
    }

    if (text.length > 0) {
        recomendacionElement.style.borderRight = '0.15em solid white'; // Añadir el cursor de escritura
        type();
    }
});


