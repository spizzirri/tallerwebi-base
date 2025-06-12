document.addEventListener("DOMContentLoaded", function () {
    // Inicializar contador del carrito al cargar cualquier página
    if (typeof inicializarContadorCarrito === 'function') {
        inicializarContadorCarrito();
    }
});

window.agregarAlCarrito = function(componenteId, cantidad = 1) {

    return fetch("/spring/agregarAlCarrito", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `componenteId=${componenteId}&cantidad=${cantidad}`
    })
        .then(response => response.json())
        .then(data => {

            if(data.success){
                mostrarMensaje(data.mensaje, 'success', componenteId);
            } else {
                mostrarMensaje(data.mensaje, 'error', componenteId);
            }

            // Actualizar contador si viene en la respuesta
            if(data.cantidadEnCarrito !== undefined) {
                actualizarContadorCarrito(data.cantidadEnCarrito);
            }

            return data;
        })
        .catch(error => {
            mostrarMensaje("Error al conectar con el servidor", 'error', componenteId);
            throw error;
        });
}

window.mostrarMensaje = function(mensaje, tipo, productId) {
    const mensajeNotificacion = document.getElementById(`mensajeNotificacion-${productId}`);

    if(mensajeNotificacion){
        mensajeNotificacion.textContent = mensaje;
        mensajeNotificacion.className = `alert alert-${tipo === 'success' ? 'success' : 'danger'} mt-2`;
        mensajeNotificacion.style.display = 'block';

        setTimeout(() => {
            mensajeNotificacion.style.display = 'none';
        }, 3000);
    } else {
        alert(mensaje);
    }
}

window.actualizarContadorCarrito = function(nuevaCantidad){
    const contadorCarrito = document.getElementById("contadorCarrito");

    if(contadorCarrito){
        contadorCarrito.textContent = nuevaCantidad;
    }
}