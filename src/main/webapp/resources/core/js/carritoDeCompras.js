document.addEventListener("DOMContentLoaded", function() {
    const boton = document.getElementById("btnAplicarDescuento");
    const input = document.getElementById("codigoInput");
    const mensajeParaAlert = document.getElementById("mensajeDescuento");
    const contenidoMensaje = document.getElementById("contenidoMensaje");

    boton.addEventListener("click", function() {
        const codigo = input.value.trim()
        if(!codigo) return;

        fetch('/spring/carritoDeCompras/aplicarDescuento', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ codigoInput: codigo }) // ¡clave y valor como JSON!
        })
            .then(response => response.json())
            .then(data => {
                contenidoMensaje.innerText = data.mensaje;
                mensajeParaAlert.classList.remove("d-none");
            })
            .catch(error => {
                contenidoMensaje.textContent = 'Hubo un error al aplicar el descuento.';
            })
    })
})