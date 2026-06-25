/* eslint-disable no-unused-vars */
const overlay = document.getElementById("modalOverlay");
document.getElementById("btnAbrirForm").addEventListener("click", () => overlay.classList.add("visible"));
document.getElementById("btnCerrar").addEventListener("click", () => overlay.classList.remove("visible"));
document.getElementById("btnCancelar").addEventListener("click", () => overlay.classList.remove("visible"));
overlay.addEventListener("click", (e) => { if (e.target === overlay) overlay.classList.remove("visible"); });


function abrirSeccionEdicion(boton) {
    const seccion = document.getElementById("seccionEdicionHijo");
    seccion.style.display = "block";

    document.getElementById("editIdHijo").value = boton.getAttribute("data-id");
    document.getElementById("editNombre").value = boton.getAttribute("data-nombre");
    document.getElementById("editApellido").value = boton.getAttribute("data-apellido");
    document.getElementById("editFecha").value = boton.getAttribute("data-fecha");
    document.getElementById("editDni").value = boton.getAttribute("data-dni");
    document.getElementById("editAnio").value = boton.getAttribute("data-anio");
    document.getElementById("editDivision").value = boton.getAttribute("data-division");

    // CORRECCIÓN SEGURA: Verificamos primero si existe el input en el HTML antes de asignarle el valor
    const inputAlias = document.getElementById('editAlias');
    if (inputAlias) {
        inputAlias.value = boton.getAttribute('data-alias') || '';
    }

    seccion.scrollIntoView({ behavior: "smooth" });
}

function cerrarSeccionEdicion() {
  document.getElementById("seccionEdicionHijo").style.display = "none";
}

function abrirModalAlias(button) {

    const hijoId = button.dataset.id;

    document.getElementById("hijoIdAlias").value = hijoId;

    document.getElementById("modalAlias").style.display = "flex";
}

function cerrarModalAlias() {
    document.getElementById("modalAlias").style.display = "none";
}