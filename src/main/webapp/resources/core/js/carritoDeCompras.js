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
});

// Boton para confirmar compra, verificando metodo de pago
document.addEventListener("DOMContentLoaded", function () {
    const formularioPago = document.getElementById("formulario-pago");

    formularioPago.addEventListener("submit", function(e) {
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

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formulario-envio');
    const loading = document.getElementById('loading');

    form.addEventListener('submit', function (e) {
        const codigoPostal = document.getElementById('codigoPostal').value.trim();
        if (window.fetch && codigoPostal) {
            e.preventDefault();
            calcularConAjax(codigoPostal);
        }
    });
});

    function calcularConAjax(codigoPostal) {
        loading.classList.remove('d-none');

        fetch(`/spring/carritoDeCompras/calcular?codigoPostal=${codigoPostal}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('costo').textContent = '$' + data.costo;
                    document.getElementById('tiempo').textContent = data.tiempo;
                    document.getElementById('zona').textContent = data.destino;

                    const totalActual = parseFloat(document.querySelector('.valorTotalDelCarrito').textContent);
                    const totalConEnvio = totalActual + data.costo;

                    let parrafoTotal = document.querySelector('.total-con-envio');

                    if (!parrafoTotal) {
                        parrafoTotal = document.createElement('p');
                        parrafoTotal.className = 'total-con-envio';

                        const botonComprar = document.getElementById('btnComprar');
                        botonComprar.parentElement.insertBefore(parrafoTotal, botonComprar.parentElement.firstChild);
                    }

                    const hayDescuento = !document.getElementById('mensajeDescuento').classList.contains('d-none');

                    if (hayDescuento) {
                        parrafoTotal.innerHTML = 'Total con envio y descuento: <span class="total-envio-valor"></span>';
                        console.log('Calculando envío CON descuento aplicado');
                    } else {
                        parrafoTotal.innerHTML = 'Total con envio: <span class="total-envio-valor"></span>';
                        console.log('Calculando envío SIN descuento');
                    }

                    parrafoTotal.querySelector('.total-envio-valor').textContent = '$' + totalConEnvio.toFixed(2);
                    parrafoTotal.style.display = 'block';

                    window.datosEnvio = {
                        costo: data.costo,
                        destino: data.destino,
                        codigoPostal: codigoPostal
                    };

                    console.log('Total final con envío:', totalConEnvio);

                } else {
                    alert('Sin cobertura para este código postal');
                    const parrafoTotal = document.querySelector('.total-con-envio');
                    if (parrafoTotal) {
                        parrafoTotal.style.display = 'none';
                    }
                    window.datosEnvio = null;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                form.submit();
            })
            .finally(() => {
                loading.classList.add('d-none');
            });
    }