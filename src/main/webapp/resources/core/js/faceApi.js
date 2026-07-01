// Variable para saber si la API está lista
let apiLista = false;

// Esta función se conecta a internet para activar tu Face-API sin bajar archivos al proyecto
async function inicializarFaceApi() {
    try {
        console.log("Conectando con el servicio de Face-API externo...");
        // Cargamos los pesos directamente desde un repositorio público en la nube
        const cdnModelos = "https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights";

        await faceapi.nets.ssdMobilenetv1.loadFromUri(cdnModelos);
        apiLista = true;
        console.log("¡Face-API lista para validar rostros!");
    } catch (err) {
        console.error("No se pudieron cargar los modelos desde el servidor externo:", err);
    }
}

// Arranca la carga apenas se abre la vista
document.addEventListener("DOMContentLoaded", inicializarFaceApi);

function resetEstadoFoto(formulario) {
    const input = formulario.querySelector(".input-foto-hijo");
    const contenedorError = formulario.querySelector(".contenedor-error-foto");
    const contenedorExito = formulario.querySelector(".contenedor-exito-foto");
    const contenedorValidando = formulario.querySelector(".contenedor-validando-foto");
    const btnQuitar = formulario.querySelector(".btn-quitar-foto");
    const btnGuardar = formulario.querySelector("button[type=\"submit\"]");

    if (input) {
        input.value = "";
    }
    if (contenedorError) contenedorError.style.display = "none";
    if (contenedorExito) contenedorExito.style.display = "none";
    if (contenedorValidando) contenedorValidando.style.display = "none";
    if (btnQuitar) btnQuitar.style.display = "none";
    if (btnGuardar) btnGuardar.disabled = false;
}

document.addEventListener("click", function(e) {
    const btn = e.target.closest(".btn-quitar-foto");
    if (!btn) return;

    e.preventDefault();
    const formulario = btn.closest("form");
    resetEstadoFoto(formulario);
});

document.addEventListener("change", async function(e) {
    if (e.target && e.target.classList.contains("input-foto-hijo")) {
        const input = e.target;
        const archivos = input.files;

        if (!archivos || archivos.length === 0) return;

        const archivo = archivos[0];
        const formulario = input.closest("form");

        const contenedorError = formulario.querySelector(".contenedor-error-foto");
        const textoError = formulario.querySelector(".texto-error-foto");
        const contenedorExito = formulario.querySelector(".contenedor-exito-foto");
        const textoExito = formulario.querySelector(".texto-exito-foto");
        const contenedorValidando = formulario.querySelector(".contenedor-validando-foto");
        const btnQuitar = formulario.querySelector(".btn-quitar-foto");
        const btnGuardar = formulario.querySelector("button[type=\"submit\"]");

        contenedorError.style.display = "none";
        contenedorExito.style.display = "none";
        contenedorValidando.style.display = "flex";
        btnQuitar.style.display = "flex";
        btnGuardar.disabled = true;

        // Si la API externa todavía no respondió o falló la conexión a internet
        if (!apiLista) {
            textoError.innerText = "El validador de rostros se está conectando al servidor. Esperá 3 segundos y volvé a subir la foto.";
            contenedorError.style.display = "flex";
            contenedorValidando.style.display = "none";
            btnGuardar.disabled = false; // Te dejamos guardar los demás datos si internet anda lento
            return;
        }

        try {
            const img = await faceapi.bufferToImage(archivo);
            const detecciones = await faceapi.detectAllFaces(img);

            if (detecciones.length === 0) {
                textoError.innerText = "No se detectó ningún rostro. Asegurate de que se vea bien de frente y con buena iluminación.";
                contenedorError.style.display = "flex";
                btnGuardar.disabled = true;
            } else if (detecciones.length > 1) {
                textoError.innerText = "Se detectó más de una persona. La foto debe ser de tipo carnet (únicamente el alumno).";
                contenedorError.style.display = "flex";
                btnGuardar.disabled = true;
            } else {
                textoExito.innerText = "La foto se validó correctamente";
                contenedorExito.style.display = "flex";
                btnGuardar.disabled = false;
            }
        } catch (error) {
            console.error("Error al procesar la imagen con face-api:", error);
            textoError.innerText = "Ocurrió un error al validar la foto. Podés quitarla con la 'X' para continuar.";
            contenedorError.style.display = "flex";
            btnGuardar.disabled = true;
        } finally {
            if (contenedorValidando) {
                contenedorValidando.style.display = "none";
            }
        }
    }
});