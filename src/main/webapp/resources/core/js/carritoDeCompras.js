// Boton para sumar cantidad de un mismo producto

document.addEventListener("DOMContentLoaded", function () {
    const boton = document.querySelectorAll(".btnSumarCantidad");

    boton.forEach(element => {
        element.addEventListener("click", function () {
            let spanCantidad = this.parentElement.querySelector(".productoCantidad");
            let idProducto = this.closest('td').dataset.id;
            let fila = this.closest('tr');
            let precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");
            let valorTotalDelCarrito = document.querySelector(".valorTotalDelCarrito");

            fetch(`/spring/carritoDeCompras/agregarMasCantidadDeUnProducto/${idProducto}`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    // Actualizar solo el valor de la cantidad
                    spanCantidad.textContent = data.cantidad;
                    precioTotalDelProducto.textContent = data.precioTotalDelProducto.toFixed(2);
                    valorTotalDelCarrito.textContent = data.valorTotal.toFixed(2);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        })
    })
})

// Boton para restar cantidad de un mismo producto

document.addEventListener("DOMContentLoaded", function () {
    const boton = document.querySelectorAll(".btnRestarCantidad");

    boton.forEach(element => {
        element.addEventListener("click", function () {
            let spanCantidad = this.parentElement.querySelector(".productoCantidad");
            let idProducto = this.closest('td').dataset.id;
            let fila = this.closest('tr');
            let precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");
            let valorTotalDelCarrito = document.querySelector(".valorTotalDelCarrito");

            fetch(`/spring/carritoDeCompras/restarCantidadDeUnProducto/${idProducto}`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.eliminado) {
                        fila.remove(); // Eliminar la fila si se eliminó el producto
                    } else {
                        // Actualizar solo el valor de la cantidad
                        spanCantidad.textContent = data.cantidad;
                        precioTotalDelProducto.textContent = data.precioTotalDelProducto.toFixed(2);
                        valorTotalDelCarrito.textContent = data.valorTotal.toFixed(2);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        })
    })
})

// Boton para eliminar un producto del carrito

document.addEventListener("DOMContentLoaded", function () {
    const boton = document.querySelectorAll(".btnEliminarProducto");

    boton.forEach(element => {
        element.addEventListener('click', function () {
                let idProductoAEliminar = this.dataset.id;

                fetch(`/spring/carritoDeCompras/eliminarProducto/${idProductoAEliminar}`, {
                    method: 'POST'
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.eliminado) {
                            this.closest('tr').remove();
                            document.querySelector(".valorTotalDelCarrito").textContent = data.valorTotal.toFixed(2);
                        }
                    })
            }
        )
    })
})

// Boton para el codigo de descuento
document.addEventListener("DOMContentLoaded", function () {
    const boton = document.getElementById("btnAplicarDescuento");
    const input = document.getElementById("codigoInput");
    const mensajeParaAlert = document.getElementById("mensajeDescuento");
    const contenidoMensaje = document.getElementById("contenidoMensaje");

    boton.addEventListener("click", function () {
        const codigo = input.value.trim()
        if (!codigo) return;

        fetch('/spring/carritoDeCompras/aplicarDescuento', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({codigoInput: codigo}) // ¡clave y valor como JSON!
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

// Boton para confirmar compra, verificando metodo de pago
document.addEventListener("DOMContentLoaded", function () {
    const botonComprar = document.getElementById("btnComprar");

    botonComprar.addEventListener("click", function (e) {
        const metodoSeleccionado = document.querySelector('input[name="metodoPago"]:checked');
        const errorDiv = document.getElementById("errorMetodoPago");
        const modal = new bootstrap.Modal(document.getElementById('miModal'));

        if (!metodoSeleccionado) {
            e.preventDefault();
            errorDiv.innerText = "Debes seleccionar un metodo de pago";
            errorDiv.classList.remove("d-none");
            return;
        }

        const formData = new URLSearchParams();
        formData.append('metodoPago', metodoSeleccionado.value);

        // aca hago el ajax a la ruta para guardar el pedido
        fetch('/spring/carritoDeCompras/formularioDePago', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (!data.success) {
                    e.preventDefault();
                    errorDiv.innerText = data.error;
                    errorDiv.classList.remove("d-none");
                } else {
                    errorDiv.classList.add("d-none");
                    data.mostrarModal;
                    modal.show();
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });
});
