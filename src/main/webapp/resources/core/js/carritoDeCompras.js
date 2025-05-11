// Boton para sumar cantidad de un mismo producto

document.addEventListener("DOMContentLoaded", function() {
    const boton = document.querySelectorAll(".btnSumarCantidad");

    boton.forEach(element => {
        element.addEventListener("click", function() {
            let spanCantidad = this.parentElement.querySelector(".productoCantidad");
            let idProducto = this.closest('td').dataset.id;
            let fila = this.closest('tr');
            let precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");
            console.log("Precio total actual del producto: ", precioTotalDelProducto);

            fetch(`/spring/carritoDeCompras/agregarMasCantidadDeUnProducto/${idProducto}`, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    // Actualizar solo el valor de la cantidad
                    spanCantidad.textContent = data.cantidad;

                    // Aquí actualizamos el precio total solo de ese producto específico
                    let nuevoPrecioTotal = data.valorTotal / data.cantidad;
                    precioTotalDelProducto.textContent = nuevoPrecioTotal.toFixed(2);

                    console.log("Nuevo valor total después de la actualización:", data.valorTotal);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        })
    })
})

// Boton para el codigo de descuento
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