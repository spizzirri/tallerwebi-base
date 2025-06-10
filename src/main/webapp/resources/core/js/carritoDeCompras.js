// Función para calcular la cantidad total desde los productos en la página
function calcularCantidadDesdeDOM() {
    let cantidadTotal = 0;
    const elementos = document.querySelectorAll('.productoCantidad');

    elementos.forEach(elemento => {
        const cantidad = parseInt(elemento.textContent) || 0;
        cantidadTotal += cantidad;
    });

    return cantidadTotal;
}

// Función para obtener la cantidad del carrito via AJAX
function obtenerCantidadCarritoAjax() {
    fetch('/spring/carritoDeCompras/cantidad')
        .then(response => response.json())
        .then(data => {
            console.log("Cantidad obtenida via AJAX:", data.cantidadEnCarrito);
            actualizarContadorCarrito(data.cantidadEnCarrito);
        })
        .catch(error => {
            console.error('Error al obtener cantidad del carrito:', error);
            // En caso de error, intentar calcular desde el DOM
            const cantidad = calcularCantidadDesdeDOM();
            actualizarContadorCarrito(cantidad);
        });
}

// Función para inicializar el contador del carrito al cargar la página
function inicializarContadorCarrito() {
    let cantidadEnCarrito = 0;

    if (typeof window.cantidadEnCarrito !== 'undefined') {
        cantidadEnCarrito = window.cantidadEnCarrito;
        console.log("Cantidad obtenida desde window:", cantidadEnCarrito);
    }
    else if (document.querySelector('.productoCantidad')) {
        cantidadEnCarrito = calcularCantidadDesdeDOM();
        console.log("Cantidad calculada desde DOM:", cantidadEnCarrito);
    }
    else {
        console.log("Obteniendo cantidad via AJAX...");
        obtenerCantidadCarritoAjax();
        return; 
    }

    console.log("Inicializando contador con cantidad:", cantidadEnCarrito);
    actualizarContadorCarrito(cantidadEnCarrito);
}

// Función global para actualizar el contador del carrito
window.actualizarContadorCarrito = function (nuevaCantidad) {
    console.log("Actualizando contador a:", nuevaCantidad);
    const contadorCarrito = document.getElementById("contadorCarrito");
    if (contadorCarrito) {
        contadorCarrito.textContent = nuevaCantidad;
        console.log("Contador actualizado exitosamente");
    } else {
        console.error("Elemento contadorCarrito no encontrado");
    }
}

// Inicializar contador cuando se carga la página
document.addEventListener("DOMContentLoaded", function () {
    console.log("DOM cargado, inicializando contador...");
    inicializarContadorCarrito();
});

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
                    actualizarContadorCarrito(data.cantidadEnCarrito);
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
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                        valorTotalDelCarrito.textContent = data.valorTotal.toFixed(2);
                    } else {
                        // Actualizar solo el valor de la cantidad
                        spanCantidad.textContent = data.cantidad;
                        precioTotalDelProducto.textContent = data.precioTotalDelProducto.toFixed(2);
                        valorTotalDelCarrito.textContent = data.valorTotal.toFixed(2);
                        actualizarContadorCarrito(data.cantidadEnCarrito);
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
                            actualizarContadorCarrito(data.cantidadEnCarrito);
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

    if (boton) {
        boton.addEventListener("click", function () {
            const codigo = input.value.trim();
            if (!codigo) return;

            fetch('/spring/carritoDeCompras/aplicarDescuento', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({codigoInput: codigo})
            })
                .then(response => response.json())
                .then(data => {

                    if (data.mensaje || data.mensajeDescuento) {
                        contenidoMensaje.innerText = data.mensaje || data.mensajeDescuento;
                        mensajeParaAlert.classList.remove("d-none");
                    }

                    if (data.valorTotal) {

                        const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
                        if (valorTotalElement) {
                            valorTotalElement.textContent = data.valorTotal.toFixed(2);
                        }

                        if (window.datosEnvio && window.datosEnvio.costo) {
                            const nuevoTotalConEnvio = data.valorTotal + window.datosEnvio.costo;
                            let parrafoTotal = document.querySelector('.total-con-envio');

                            if (parrafoTotal) {
                                parrafoTotal.innerHTML = 'Total con envio y descuento: <span class="total-envio-valor"></span>';
                                parrafoTotal.querySelector('.total-envio-valor').textContent = '$' + nuevoTotalConEnvio.toFixed(2);
                                parrafoTotal.style.display = 'block';

                                console.log('Total recalculado con descuento y envío:', nuevoTotalConEnvio);
                            }
                        }
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    contenidoMensaje.textContent = 'Hubo un error al aplicar el descuento.';
                    mensajeParaAlert.classList.remove("d-none");
                });
        });
    }
});

// Boton para confirmar compra, verificando metodo de pago
document.addEventListener("DOMContentLoaded", function () {
    const formularioPago = document.getElementById("formulario-pago");

    if (formularioPago) {
        formularioPago.addEventListener("submit", function (e) {
            e.preventDefault();

            let metodoSeleccionado = document.querySelector('input[name="metodoPago"]:checked');
            if (metodoSeleccionado === null) {
                const errorDiv = document.getElementById("errorMetodoPago");
                errorDiv.innerText = "Debes seleccionar un método de pago";
                errorDiv.classList.remove("d-none");
                return false;
            }

            const errorDiv = document.getElementById("errorMetodoPago");
            errorDiv.classList.add("d-none");

            // mostrar spinner
            const btnComprar = document.getElementById("btnComprar");
            btnComprar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Redirigiendo a MercadoPago...';
            btnComprar.disabled = true;

            const params = new URLSearchParams();
            params.append('metodoPago', metodoSeleccionado.value);

            fetch('/spring/carritoDeCompras/formularioPago', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        crearFormularioMercadoPago(data);
                    } else {
                        btnComprar.innerHTML = 'Finalizar compra';
                        btnComprar.disabled = false;
                        errorDiv.innerText = data.error;
                        errorDiv.classList.remove("d-none");
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    btnComprar.innerHTML = 'Finalizar compra';
                    btnComprar.disabled = false;
                    errorDiv.innerText = "Error al procesar el pago";
                    errorDiv.classList.remove("d-none");
                });
        });
    }
});

function crearFormularioMercadoPago(data) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/spring/checkout/create-payment';
    form.innerHTML += `<input type="hidden" name="metodoDePago" value="mercadoPago">`;

    let costoEnvio = data.costoEnvio;
    if (!costoEnvio && window.datosEnvio) {
        costoEnvio = window.datosEnvio.costo;
    }

    if (costoEnvio && costoEnvio > 0) {
        form.innerHTML += `<input type="hidden" name="costoEnvio" value="${costoEnvio}">`;
        console.log('Enviando costo de envío a MP:', costoEnvio);
    }

    document.body.appendChild(form);
    form.submit();
}

// Formulario de envío
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formulario-envio');
    const loading = document.getElementById('loading');

    if (form) {
        form.addEventListener('submit', function (e) {
            const codigoPostal = document.getElementById('codigoPostal').value.trim();
            if (window.fetch && codigoPostal) {
                e.preventDefault();
                calcularConAjax(codigoPostal);
            }
        });
    }
});

function calcularConAjax(codigoPostal) {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.classList.remove('d-none');
    }

    fetch(`/spring/carritoDeCompras/calcular?codigoPostal=${codigoPostal}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // envio
                document.getElementById('alertaSinCobertura').classList.add('d-none');
                document.getElementById('costo').textContent = '$' + data.costo;
                document.getElementById('tiempo').textContent = data.tiempo;
                document.getElementById('zona').textContent = data.destino;

                const totalActual = parseFloat(document.querySelector('.valorTotalDelCarrito').textContent);
                const totalConEnvio = totalActual + data.costo;

                let parrafoTotal = document.querySelector('.total-con-envio');
                if (!parrafoTotal) {
                    parrafoTotal = document.createElement('p');
                    parrafoTotal.className = 'total-con-envio';
                    document.getElementById('btnComprar').parentElement.insertBefore(parrafoTotal, document.getElementById('btnComprar').parentElement.firstChild);
                }

                //descuento
                const hayDescuento = !document.getElementById('mensajeDescuento').classList.contains('d-none');
                parrafoTotal.innerHTML = `Total con envío${hayDescuento ? ' y descuento' : ''}: <span class="total-envio-valor">$${totalConEnvio.toFixed(2)}</span>`;
                parrafoTotal.style.display = 'block';

                //mp
                window.datosEnvio = {
                    costo: data.costo,
                    destino: data.destino,
                    codigoPostal: codigoPostal
                };

            } else {
                document.getElementById('alertaSinCobertura').classList.remove('d-none');

                const parrafoTotal = document.querySelector('.total-con-envio');
                if (parrafoTotal) {
                    parrafoTotal.style.display = 'none';
                }
                window.datosEnvio = null;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const form = document.getElementById('formulario-envio');
            if (form) {
                form.submit();
            }
        })
        .finally(() => {
            if (loading) {
                loading.classList.add('d-none');
            }
        });
}
