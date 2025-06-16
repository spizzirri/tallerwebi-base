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
            actualizarContadorCarrito(data.cantidadEnCarrito);
        })
        .catch(error => {
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
    } else if (document.querySelector('.productoCantidad')) {
        cantidadEnCarrito = calcularCantidadDesdeDOM();
    } else {
        obtenerCantidadCarritoAjax();
        return;
    }

    actualizarContadorCarrito(cantidadEnCarrito);
}

// Función global para actualizar el contador del carrito
window.actualizarContadorCarrito = function (nuevaCantidad) {
    const contadorCarrito = document.getElementById("contadorCarrito");
    if (contadorCarrito) {
        contadorCarrito.textContent = nuevaCantidad;
    }
}

// Inicializar contador cuando se carga la página
document.addEventListener("DOMContentLoaded", function () {
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
                    if (data.cantidad !== undefined) {
                        spanCantidad.textContent = data.cantidad;
                    }

                    precioTotalDelProducto.innerHTML = `$${data.precioTotalDelProducto.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;
                    valorTotalDelCarrito.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;

                    if (data.cantidadEnCarrito !== undefined && data.cantidadEnCarrito !== null) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                })
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
                        fila.remove();
                        if (data.valorTotal !== undefined && data.valorTotal !== null) {
                            valorTotalDelCarrito.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;
                        }
                    } else {
                        if (data.cantidad !== undefined) {
                            spanCantidad.textContent = data.cantidad;
                        }
                        precioTotalDelProducto.innerHTML = `$${data.precioTotalDelProducto.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;
                        valorTotalDelCarrito.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;

                    }

                    if (data.cantidadEnCarrito !== undefined && data.cantidadEnCarrito !== null) {
                        actualizarContadorCarrito(data.cantidadEnCarrito);
                    }
                })
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
                        document.querySelector(".valorTotalDelCarrito").innerHTML = `$${data.valorTotal.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;

                        // Actualizar el contador del carrito
                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    }
                })
        })
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
                            if (!valorTotalElement.dataset.valorOriginal) {
                                const valorActualTexto = valorTotalElement.textContent || valorTotalElement.innerHTML;
                                const valorLimpio = parseFloat(valorActualTexto.replace(/[^\d.-]/g, ''));
                                valorTotalElement.dataset.valorOriginal = valorLimpio;
                            }

                            valorTotalElement.dataset.valorConDescuento = data.valorTotal;

                            valorTotalElement.innerHTML = `$${data.valorTotal.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`;
                        }
                    }
                })
                .catch(error => {
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
                errorDiv.innerText = "Debes seleccionar un metodo de pago";
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
    }

    // ✅ Enviar información del descuento si existe
    const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
    const valorOriginal = valorTotalElement.dataset.valorOriginal;
    const valorConDescuento = valorTotalElement.dataset.valorConDescuento;

    if (valorOriginal && valorConDescuento && valorOriginal !== valorConDescuento) {
        form.innerHTML += `<input type="hidden" name="totalOriginal" value="${valorOriginal}">`;
        form.innerHTML += `<input type="hidden" name="totalConDescuento" value="${valorConDescuento}">`;

        console.log('📋 Enviando a MercadoPago:');
        console.log('Total original:', valorOriginal);
        console.log('Total con descuento:', valorConDescuento);
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
                document.getElementById('alertaSinCobertura').classList.add('d-none');
                document.getElementById('costo').textContent = '$' + data.costo;
                document.getElementById('tiempo').textContent = data.tiempo;
                document.getElementById('zona').textContent = data.destino;

                // ✅ SOLUCIÓN: Mejor parsing que detecta el formato
                const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
                const totalTexto = valorTotalElement.textContent || '$0';

                let totalActual;

                if (totalTexto.includes(',') && totalTexto.lastIndexOf(',') > totalTexto.lastIndexOf('.')) {
                    totalActual = parseFloat(
                        totalTexto
                            .replace('$', '')
                            .replace(/\./g, '')
                            .replace(',', '.')
                    );
                } else {
                    // Formato estándar: $22650.0 o $22650
                    totalActual = parseFloat(
                        totalTexto.replace(/[^\d.-]/g, '')
                    );
                }

                const costoEnvio = parseFloat(data.costo);

                console.log('🔍 Debug info:');
                console.log('Total texto:', totalTexto);
                console.log('Total parseado:', totalActual);
                console.log('Costo envío:', costoEnvio);

                const totalConEnvio = (!isNaN(totalActual) && !isNaN(costoEnvio)) ? totalActual + costoEnvio : 0;

                console.log('Total con envío:', totalConEnvio);

                let parrafoTotal = document.querySelector('.total-con-envio');
                if (!parrafoTotal) {
                    parrafoTotal = document.createElement('p');
                    parrafoTotal.className = 'total-con-envio';
                    document.getElementById('btnComprar').parentElement.insertBefore(parrafoTotal, document.getElementById('btnComprar').parentElement.firstChild);
                }

                const hayDescuento = !document.getElementById('mensajeDescuento').classList.contains('d-none');
                parrafoTotal.innerHTML = `Total con envio${hayDescuento ? ' y descuento' : ''}: <span class="total-envio-valor">$${totalConEnvio.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</span>`;
                parrafoTotal.style.display = 'block';

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
            console.error('Error en calcularConAjax:', error);
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