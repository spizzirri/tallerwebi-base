const overlay = document.getElementById('modalOverlay');
document.getElementById('btnAbrirForm').addEventListener('click', () => overlay.classList.add('visible'));
document.getElementById('btnCerrar').addEventListener('click', () => overlay.classList.remove('visible'));
document.getElementById('btnCancelar').addEventListener('click', () => overlay.classList.remove('visible'));
overlay.addEventListener('click', (e) => { if (e.target === overlay) overlay.classList.remove('visible'); });

function abrirSeccionEdicion(boton) {
    // 1. Mostrar la sección contenedora
    const seccion = document.getElementById('seccionEdicionHijo');
    seccion.style.display = 'block';

    // 2. Mapear los data-attributes a los inputs del formulario de edición
    document.getElementById('editIdHijo').value = boton.getAttribute('data-id');
    document.getElementById('editNombre').value = boton.getAttribute('data-nombre');
    document.getElementById('editApellido').value = boton.getAttribute('data-apellido');
    document.getElementById('editFecha').value = boton.getAttribute('data-fecha');
    document.getElementById('editDni').value = boton.getAttribute('data-dni');

    // 3. Mapear los Selects (Año y división)
    document.getElementById('editAnio').value = boton.getAttribute('data-anio');
    document.getElementById('editDivision').value = boton.getAttribute('data-division');

    // Scrollear suavemente hasta la sección si es necesario
    seccion.scrollIntoView({ behavior: 'smooth' });
}

function cerrarSeccionEdicion() {
    document.getElementById('seccionEdicionHijo').style.display = 'none';
}

function abrirModalAlias(button) {

    const hijoId = button.dataset.id;

    document.getElementById("hijoIdAlias").value = hijoId;

    document.getElementById("modalAlias").style.display = "flex";
}

function cerrarModalAlias() {
    document.getElementById("modalAlias").style.display = "none";
}