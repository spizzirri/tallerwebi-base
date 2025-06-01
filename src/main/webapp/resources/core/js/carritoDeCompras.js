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

function extraerProductosDelCarrito() {
    // Seleccionar todas las filas de productos
    const filasProductos = document.querySelectorAll("tbody tr");

    // Array para almacenar la información de los productos
    const productos = [];

    // Recorrer cada fila y extraer la información
    filasProductos.forEach(fila => {
        // Extraer el nombre del producto (primer td de la fila)
        const nombre = fila.querySelector("td:nth-child(1)").textContent.trim();

        // Extraer el precio (elemento con la clase precioTotalDelProducto)
        const precio = parseFloat(fila.querySelector(".precioTotalDelProducto").textContent.trim());

        // Extraer la cantidad (elemento con la clase productoCantidad)
        const cantidad = parseInt(fila.querySelector(".productoCantidad").textContent.trim());

        // Extraer el ID del producto del atributo data-id
        const id = fila.querySelector("td[data-id]").getAttribute("data-id");

        // Agregar el producto al array
        productos.push({
            id: id,
            nombre: nombre,
            precio: precio,
            cantidad: cantidad,
            precioUnitario: precio / cantidad // Calcular el precio unitario
        });
    });

    return productos;
}

// Boton para confirmar compra, verificando metodo de pago
document.addEventListener("DOMContentLoaded", function () {
    const botonComprar = document.getElementById("btnComprar");
    const formularioPago = document.getElementById("formulario-pago");
    const camposProductos = document.getElementById("campos-productos");
    const codigoDescuentoInput = document.getElementById("codigoInput");
    const codigoDescuentoHidden = document.getElementById("codigoDescuentoHidden");

    // Preparar el formulario antes de enviarlo
    formularioPago.addEventListener("submit", function(e) {
        e.preventDefault(); // Prevenir envío por defecto

        // Verificar si se seleccionó un método de pago
        let metodoSeleccionado = document.querySelector('input[name="metodoPago"]:checked');
        if (metodoSeleccionado === null) {
            const errorDiv = document.getElementById("errorMetodoPago");
            errorDiv.innerText = "Debes seleccionar un método de pago";
            errorDiv.classList.remove("d-none");
            return false;
        }

        // Limpiar cualquier mensaje de error previo
        const errorDiv = document.getElementById("errorMetodoPago");
        errorDiv.classList.add("d-none");

        // Actualizar el campo de código de descuento
        if (codigoDescuentoHidden) {
            codigoDescuentoHidden.value = codigoDescuentoInput.value;
        }

        // Obtener productos del carrito
        const productos = extraerProductosDelCarrito();

        // Limpiar campos de productos anteriores
        camposProductos.innerHTML = '';

        // Crear campos ocultos para cada producto
        productos.forEach((producto, index) => {
            // Crear campos para cada propiedad del producto
            camposProductos.innerHTML += `
                <input type="hidden" name="productoDtoList[${index}].id" value="${producto.id}">
                <input type="hidden" name="productoDtoList[${index}].nombre" value="${producto.nombre}">
                <input type="hidden" name="productoDtoList[${index}].precio" value="${producto.precio}">
                <input type="hidden" name="productoDtoList[${index}].cantidad" value="${producto.cantidad}">
                <input type="hidden" name="productoDtoList[${index}].precioUnitario" value="${producto.precioUnitario}">
            `;
        });

        // Enviar el formulario
        formularioPago.submit();
    });
});


document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('formulario-envio');
    const loading = document.getElementById('loading');

    // Interceptar el envío del formulario para hacerlo con AJAX
    form.addEventListener('submit', function(e) {
        const codigoPostal = document.getElementById('codigoPostal').value.trim();
        const retiroEnLocal = document.getElementById('retiroEnLocal').checked;

        // Si JavaScript está disponible, prevenir submit normal y usar AJAX
        if (window.fetch && codigoPostal && !retiroEnLocal) {
            e.preventDefault();
            calcularConAjax(codigoPostal);
        }
        // Si no, deja que funcione normalmente con Thymeleaf
    });

    function calcularConAjax(codigoPostal) {
        loading.classList.remove('d-none');

        // Llamada AJAX a tu endpoint JSON
        fetch(`/spring/carritoDeCompras/calcular?codigoPostal=${codigoPostal}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Actualizar los valores sin recargar la página
                    document.getElementById('costo').textContent = '$' + data.costo;
                    document.getElementById('tiempo').textContent = data.tiempo;
                    document.getElementById('zona').textContent = data.destino;
                } else {
                    alert('Sin cobertura para este código postal');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                // Si falla AJAX, enviar formulario normalmente
                form.submit();
            })
            .finally(() => {
                loading.classList.add('d-none');
            });
    }
});
