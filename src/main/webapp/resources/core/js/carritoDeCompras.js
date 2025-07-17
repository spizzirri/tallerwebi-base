function calcularCantidadDesdeDOM() {
    let cantidadTotal = 0;
    const elementos = document.querySelectorAll('.productoCantidad');

    elementos.forEach(elemento => {
        const cantidad = parseInt(elemento.textContent) || 0;
        cantidadTotal += cantidad;
    });

    return cantidadTotal;
}

function obtenerCantidadCarritoAjax() {
    fetch('/carritoDeCompras/cantidad')
        .then(response => response.json())
        .then(data => {
            actualizarContadorCarrito(data.cantidadEnCarrito);
        })
        .catch(error => {
            const cantidad = calcularCantidadDesdeDOM();
            actualizarContadorCarrito(cantidad);
        });
}

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

document.addEventListener("DOMContentLoaded", function () {
    inicializarContadorCarrito();
});

document.addEventListener("DOMContentLoaded", function () {
    const boton = document.getElementById("btnAplicarDescuento");
    const input = document.getElementById("codigoInput");
    const mensajeParaAlert = document.getElementById("mensajeDescuento");
    const contenidoMensaje = document.getElementById("contenidoMensaje");

    if (boton) {
        boton.addEventListener("click", function () {
            const codigo = input.value.trim();
            if (!codigo) return;

            fetch('/carritoDeCompras/aplicarDescuento', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ codigoInput: codigo })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.mensaje || data.mensajeDescuento) {
                        contenidoMensaje.innerText = data.mensaje || data.mensajeDescuento;
                        mensajeParaAlert.classList.remove("d-none");
                    }

                    if (data.valorTotal) {
                        const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
                        valorTotalElement.dataset.valorConDescuento = data.valorTotal;
                        valorTotalElement.innerHTML = `$${data.valorTotal}`;
                    }
                });
        });
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const formularioPago = document.getElementById("formulario-pago");

    if (formularioPago) {
        formularioPago.addEventListener("submit", function (e) {
            e.preventDefault();

            const formaDeEntrega = document.querySelector('input[name="tipoEntrega"]:checked')?.value;
            const codigoPostal = document.getElementById("codigoPostal").value;
            const metodoSeleccionado = document.querySelector('input[name="metodoPago"]:checked');
            const errorDiv = document.getElementById("errorMetodoPago");
            const btnComprar = document.getElementById("btnComprar");

            if (!metodoSeleccionado) {
                errorDiv.innerText = "Debes seleccionar un metodo de pago";
                errorDiv.classList.remove("d-none");
                return false;
            }

            errorDiv.classList.add("d-none");

            const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
            const valorConDescuento = valorTotalElement?.dataset?.valorConDescuento || "";

            const params = new URLSearchParams();
            params.append('metodoPago', metodoSeleccionado.value);
            params.append('formaEntrega', formaDeEntrega);
            params.append('codigoPostal', codigoPostal);
            params.append('totalConDescuento', valorConDescuento);


            fetch('/carritoDeCompras/formularioPago', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params
            })
                .then(response => response.json())
                .then(data => {
                    console.log('Respuesta del servidor:', data); // Debug

                    if (data.redirect) {
                        console.log('Redirigiendo a:', data.redirect);
                        window.location.href = data.redirect;
                        return;
                    }

                    if (data.success && metodoSeleccionado.value === "mercadoPago") {
                        crearFormularioMercadoPago(data);
                        btnComprar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Redirigiendo a mercado pago...';
                        btnComprar.disabled = true;
                    }
                    if ( data.success && metodoSeleccionado.value === "tarjetaCredito" && data.redirect ) {
                        window.location.href = data.redirect;
                        btnComprar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Redirigiendo a tarjeta de credito...';
                        btnComprar.disabled = true;
                    }

                    if ( data.success && metodoSeleccionado.value === "efectivo" && data.redirect ) {
                        window.location.href = data.redirect;
                        btnComprar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Redirigiendo';
                        btnComprar.disabled = true;
                    }

                    else if (!data.success && data.error) {
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
    form.action = '/checkout/create-payment';
    form.innerHTML += `<input type="hidden" name="metodoDePago" value="mercadoPago">`;

    let costoEnvio = data.costoEnvio;
    if (!costoEnvio && window.datosEnvio) {
        costoEnvio = window.datosEnvio.costo;
    }


    if (costoEnvio && costoEnvio > 0) {
        form.innerHTML += `<input type="hidden" name="costoEnvio" value="${costoEnvio}">`;
    }
    /**
    const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
    const valorOriginal = document.getElementById("totalOriginal").value;
    const valorConDescuento = valorTotalElement.dataset.valorConDescuento;
    console.log(valorConDescuento);

    if (valorOriginal && valorConDescuento && valorOriginal !== valorConDescuento) {
         form.innerHTML += `<input type="hidden" name="totalOriginal" value="` + valorOriginal + `">`;
       form.innerHTML += `<input type="hidden" name="totalConDescuento" value="`+ valorConDescuento + `">`;
    }
    **/

    console.log(form);
    document.body.appendChild(form);
    form.submit();
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formulario-envio');

    if (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            const codigoPostal = document.getElementById('codigoPostal').value.trim();
            if (window.fetch && codigoPostal) {
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

    fetch(`/carritoDeCompras/calcularEnvio?codigoPostal=${codigoPostal}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('alertaSinCobertura').classList.add('d-none');
                document.getElementById('costo').textContent = formatearPrecio(data.costo);
                document.getElementById('tiempo').textContent = data.tiempo;
                document.getElementById('zona').textContent = data.destino;

                const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
                const totalTexto = valorTotalElement.textContent || '$0';

                let totalActual;
                if (totalTexto.includes(',') && totalTexto.lastIndexOf(',') > totalTexto.lastIndexOf('.')) {
                    totalActual = parseFloat(totalTexto.replace('$', '').replace(/\./g, '').replace(',', '.'));
                } else {
                    totalActual = parseFloat(totalTexto.replace(/[^\d.-]/g, ''));
                }
                const costoEnvio = parseFloat(data.costo);
                const totalConEnvio = (!isNaN(totalActual) && !isNaN(costoEnvio)) ? formatearPrecio(totalActual + costoEnvio) : 0;

                let parrafoTotal = document.querySelector('.total-con-envio');
                if (!parrafoTotal) {
                    parrafoTotal = document.createElement('p');
                    parrafoTotal.className = 'total-con-envio';
                    parrafoTotal.style.fontSize = "25px";
                    parrafoTotal.style.fontWeight = "bold";


                    document.getElementById('btnComprar').parentElement.insertBefore(parrafoTotal, document.getElementById('btnComprar').parentElement.firstChild);
                }

                const hayDescuento = !document.getElementById('mensajeDescuento').classList.contains('d-none');
                parrafoTotal.innerHTML = `Total con envio${hayDescuento ? ' y descuento' : ''}: <span class="total-envio-valor">${totalConEnvio}</span>`;
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

function formatearPrecio(valor) {
    if (typeof valor === 'string') {
        valor = parseFloat(valor.replace(/\./g, '').replace(',', '.'));
    }

    if (isNaN(valor)) return '$0,00';

    return new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency: 'ARS',
        minimumFractionDigits: 2
    }).format(valor);
}

document.addEventListener("DOMContentLoaded", function () {
    const radioRetiro = document.getElementById("retiro");
    const radioEnvio = document.getElementById("envio");
    const seccionEnvio = document.getElementById("seccion-envio");

    function actualizarVisibilidadEnvio() {
        if (radioEnvio.checked) {
            seccionEnvio.style.display = "block";
        } else {
            seccionEnvio.style.display = "none";
        }
    }

    radioRetiro.addEventListener("change", actualizarVisibilidadEnvio);
    radioEnvio.addEventListener("change", actualizarVisibilidadEnvio);

    actualizarVisibilidadEnvio(); // Para que tome el estado inicial
});