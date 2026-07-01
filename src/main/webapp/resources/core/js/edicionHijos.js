/* eslint-disable no-unused-vars */
const overlay = document.getElementById("modalOverlay");
const formAgregarHijo = overlay.querySelector("form.form-hijo");
const formEditarHijo = document.getElementById("seccionEdicionHijo").querySelector("form.form-hijo");

document.getElementById("btnAbrirForm").addEventListener("click", () => {
    resetEstadoFoto(formAgregarHijo);
    overlay.classList.add("visible");
});
document.getElementById("btnCerrar").addEventListener("click", () => {
    resetEstadoFoto(formAgregarHijo);
    overlay.classList.remove("visible");
});
document.getElementById("btnCancelar").addEventListener("click", () => {
    resetEstadoFoto(formAgregarHijo);
    overlay.classList.remove("visible");
});
overlay.addEventListener("click", (e) => {
    if (e.target === overlay) {
        resetEstadoFoto(formAgregarHijo);
        overlay.classList.remove("visible");
    }
});

function abrirSeccionEdicion(boton) {
    const seccion = document.getElementById("seccionEdicionHijo");
    resetEstadoFoto(formEditarHijo);
    seccion.style.display = "block";
    document.getElementById("editIdHijo").value = boton.getAttribute("data-id");
    document.getElementById("editNombre").value = boton.getAttribute("data-nombre");
    document.getElementById("editApellido").value = boton.getAttribute("data-apellido");
    document.getElementById("editFecha").value = boton.getAttribute("data-fecha");
    document.getElementById("editDni").value = boton.getAttribute("data-dni");
    document.getElementById("editAnio").value = boton.getAttribute("data-anio");
    document.getElementById("editDivision").value = boton.getAttribute("data-division");
    const inputAlias = document.getElementById("editAlias");
    if (inputAlias) {
        inputAlias.value = boton.getAttribute("data-alias") || "";
    }
    seccion.scrollIntoView({ behavior: "smooth" });
}

function cerrarSeccionEdicion() {
    resetEstadoFoto(formEditarHijo);
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