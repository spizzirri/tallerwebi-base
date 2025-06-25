document.addEventListener("DOMContentLoaded", function () {
    // Inicializar contador del carrito al cargar cualquier página
    if (typeof inicializarContadorCarrito === 'function') {
        inicializarContadorCarrito();
    }
    actualizarResumenCarrito();
});

window.agregarAlCarrito = function (componenteId, cantidad = 1) {

    return fetch("/spring/agregarAlCarrito", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `componenteId=${componenteId}&cantidad=${cantidad}`
    })
        .then(response => response.json())
        .then(data => {

            if (data.success) {
                mostrarMensaje(data.mensaje, 'success', componenteId);
            } else {
                mostrarMensaje(data.mensaje, 'error', componenteId);
            }

            // Actualizar contador si viene en la respuesta
            if (data.cantidadEnCarrito !== undefined) {
                actualizarContadorCarrito(data.cantidadEnCarrito);
            }
            actualizarResumenCarrito();

            return data;
        })
        .catch(error => {
            mostrarMensaje("Error al conectar con el servidor", 'error', componenteId);
            throw error;
        });
}

window.actualizarResumenCarrito = function () {
    fetch('/spring/fragments/fragments')
        .then(res => res.text())
        .then(html => {
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const nuevoResumen = doc.querySelector('#resumenCarrito');
            const resumenActual = document.getElementById('resumenCarrito');

            if (resumenActual && nuevoResumen) {
                resumenActual.innerHTML = nuevoResumen.innerHTML;

                asignarEventosDelResumenCarrito();
            }
        })
        .catch(err => console.error("Error al recargar resumen del carrito", err));
}

//reusar los metodos de la vista del carrito para el resumen
window.asignarEventosDelResumenCarrito = function () {
    document.querySelectorAll('.btnRestarCantidad').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.closest('td').dataset.id;
            fetch(`/spring/carritoDeCompras/restarCantidadDeUnProducto/${id}`, {method: 'POST'})
                .then(res => res.json())
                .then(data => {  // ← Cambiar () por (data)
                    actualizarResumenCarrito();
                    if (data.cantidadEnCarrito !== undefined) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    });

    document.querySelectorAll('.btnSumarCantidad').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.closest('td').dataset.id;
            fetch(`/spring/carritoDeCompras/agregarMasCantidadDeUnProducto/${id}`, {method: 'POST'})
                .then(res => res.json())
                .then(data => {  // ← Cambiar () por (data)
                    actualizarResumenCarrito();
                    if (data.cantidadEnCarrito !== undefined) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    });

    document.querySelectorAll('.btnEliminarProducto').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            fetch(`/spring/carritoDeCompras/eliminarProducto/${id}`, {method: 'POST'})
                .then(res => res.json())
                .then(data => {  // ← Cambiar () por (data)
                    actualizarResumenCarrito();
                    if (data.cantidadEnCarrito !== undefined) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    });
}

//mostrar mensaje al agregar producto al carrito
window.mostrarMensaje = function (mensaje, tipo, productId) {
    const mensajeNotificacion = document.getElementById(`mensajeNotificacion-${productId}`);

    if (mensajeNotificacion) {
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

window.actualizarContadorCarrito = function (nuevaCantidad) {
    const contadorCarrito = document.getElementById("contadorCarrito");

    if (contadorCarrito) {
        contadorCarrito.textContent = nuevaCantidad;
    }
}

document.getElementById('abrirResumenCarrito').addEventListener('click', () => {
    document.getElementById('resumenCarrito').classList.add('abierto');
});

document.addEventListener('click', (e) => {
    if (e.target.id === 'cerrarResumenCarrito') {
        document.getElementById('resumenCarrito').classList.remove('abierto');
    }
});
