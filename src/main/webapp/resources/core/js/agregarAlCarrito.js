// Evitar múltiples inicializaciones
if (!window.eventosCarritoAsignados) {
    window.eventosCarritoAsignados = true;

    document.addEventListener("DOMContentLoaded", function () {
        if (typeof inicializarContadorCarrito === 'function') {
            inicializarContadorCarrito();
        }
        actualizarResumenCarrito();

        // Asignar eventos para la vista principal del carrito
        asignarEventosVistaCarrito();
    });
}

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

function asignarEventosVistaCarrito() {
    console.log("🔍 Asignando eventos vista carrito...");

    document.querySelectorAll(".btnSumarCantidad").forEach(element => {
        console.log("🔍 Botón sumar encontrado:", element);

        if (!element.dataset.eventoAsignado) {
            element.dataset.eventoAsignado = 'true';
            element.addEventListener("click", function () {

                let spanCantidad = this.parentElement.querySelector(".productoCantidad");
                let idProducto = this.closest('td').dataset.id;
                let fila = this.closest('tr');
                let precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");
                let valorTotalDelCarrito = document.querySelector(".valorTotalDelCarrito");

                fetch(`/spring/carritoDeCompras/agregarMasCantidadDeUnProducto/${idProducto}`, {
                    method: 'POST'
                })
                    .then(response => {
                        return response.json();
                    })
                    .then(data => {

                        if (data.cantidad !== undefined) {
                            spanCantidad.textContent = data.cantidad;
                        }
                        precioTotalDelProducto.innerHTML = `$${data.precioTotalDelProducto.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;

                        // valorTotalDelCarrito.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;
                        document.querySelectorAll(".valorTotalDelCarrito").forEach((el, index) => {
                            el.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;
                        });
                        if (data.cantidadEnCarrito !== undefined && data.cantidadEnCarrito !== null) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });
}

document.querySelectorAll(".btnRestarCantidad").forEach(element => {
    if (!element.dataset.eventoAsignado) {
        element.dataset.eventoAsignado = 'true';
        element.addEventListener("click", function () {
            let spanCantidad = this.parentElement.querySelector(".productoCantidad");
            let idProducto = this.closest('td').dataset.id;
            let fila = this.closest('tr');
            let precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");

            fetch(`/spring/carritoDeCompras/restarCantidadDeUnProducto/${idProducto}`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.eliminado) {
                        fila.remove();
                        if (data.valorTotal !== undefined && data.valorTotal !== null) {
                            // CAMBIAR: Actualizar TODOS los elementos
                            document.querySelectorAll(".valorTotalDelCarrito").forEach((el, index) => {
                                el.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;
                            });
                        }
                    } else {
                        if (data.cantidad !== undefined) {
                            spanCantidad.textContent = data.cantidad;
                        }
                        precioTotalDelProducto.innerHTML = `$${data.precioTotalDelProducto.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;

                        // CAMBIAR: Actualizar TODOS los elementos
                        document.querySelectorAll(".valorTotalDelCarrito").forEach((el, index) => {
                            el.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;
                        });
                    }
                    if (data.cantidadEnCarrito !== undefined && data.cantidadEnCarrito !== null) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    }
});

document.querySelectorAll(".btnEliminarProducto").forEach(element => {
    if (!element.dataset.eventoAsignado) {
        element.dataset.eventoAsignado = 'true';
        element.addEventListener('click', function () {
            let idProductoAEliminar = this.dataset.id;

            fetch(`/spring/carritoDeCompras/eliminarProducto/${idProductoAEliminar}`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.eliminado) {
                        this.closest('tr').remove();

                        // CAMBIAR: Actualizar TODOS los elementos
                        document.querySelectorAll(".valorTotalDelCarrito").forEach((el, index) => {
                            el.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', {minimumFractionDigits: 2})}`;
                        });

                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    }
                });
        });
    }
});

window.asignarEventosDelResumenCarrito = function () {
    document.querySelectorAll('#resumenCarrito .btnRestarCantidad').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.closest('td').dataset.id;
            fetch(`/spring/carritoDeCompras/restarCantidadDeUnProducto/${id}`, {method: 'POST'})
                .then(res => res.json())
                .then(data => {
                    actualizarResumenCarrito();
                    if (data.cantidadEnCarrito !== undefined) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    });

    document.querySelectorAll('#resumenCarrito .btnSumarCantidad').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.closest('td').dataset.id;
            fetch(`/spring/carritoDeCompras/agregarMasCantidadDeUnProducto/${id}`, {method: 'POST'})
                .then(res => res.json())
                .then(data => {
                    actualizarResumenCarrito();
                    if (data.cantidadEnCarrito !== undefined) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    });

    document.querySelectorAll('#resumenCarrito .btnEliminarProducto').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            fetch(`/spring/carritoDeCompras/eliminarProducto/${id}`, {method: 'POST'})
                .then(res => res.json())
                .then(data => {
                    actualizarResumenCarrito();
                    if (data.cantidadEnCarrito !== undefined) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                });
        });
    });
}

document.getElementById('abrirResumenCarrito').addEventListener('click', () => {
    document.getElementById('resumenCarrito').classList.add('abierto');
});

document.addEventListener('click', (e) => {
    const resumenCarrito = document.getElementById('resumenCarrito');
    const abrirBtn = document.getElementById('abrirResumenCarrito');

    if (e.target.id === 'cerrarResumenCarrito') {
        resumenCarrito.classList.remove('abierto');
        return;
    }

    if (resumenCarrito && resumenCarrito.classList.contains('abierto')) {
        if (!resumenCarrito.contains(e.target) && !abrirBtn.contains(e.target)) {
            resumenCarrito.classList.remove('abierto');
        }
    }
});