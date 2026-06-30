/* eslint-disable no-unused-vars */
function cambiar(btn, delta) {
  const control = btn.parentElement;
  const producto = btn.closest("[data-producto-id]");
  const span = control.querySelector(".qty-num");
  const input = control.querySelector(".qty-input");
  const stock = parseInt(control.dataset.stock);
  const totalActual = Array.from(producto.querySelectorAll(".qty-num"))
    .reduce((sum, s) => sum + parseInt(s.textContent), 0);
  const valorActual = parseInt(span.textContent);
  const nuevoValor = valorActual + delta;
  if (nuevoValor < 0) return;
  if (delta > 0 && totalActual >= stock) return;
  span.textContent = nuevoValor;
  input.value = nuevoValor;
  actualizarEstadoBotonConfirmar();
}
function eliminarFila(btn) {
  const producto = btn.closest("[data-producto-id]");
  const productoId = producto.dataset.productoId;
  fetch("/spring/carrito/eliminar", {
    method: "POST",
    headers: {"Content-Type": "application/x-www-form-urlencoded"},
    body: "productoId=" + productoId
  }).then(() => {
    producto.remove();
    actualizarEstadoBotonConfirmar();
  });
}
function actualizarEstadoBotonConfirmar() {
  const btnConfirmar = document.getElementById("btnConfirmarPedido");
  if (!btnConfirmar) return;
  const cantidades = document.querySelectorAll(".qty-input");
  const hayAlgunaCantidad = Array.from(cantidades)
    .some((input) => parseInt(input.value) > 0);
  btnConfirmar.disabled = !hayAlgunaCantidad;
}

function configurarFechaRetiro() {
  const fecha = document.getElementById("fechaRetiro");

  if (!fecha) {
    return;
  }

  const manana = new Date();
  manana.setDate(manana.getDate() + 1);

  const anio = manana.getFullYear();
  const mes = String(manana.getMonth() + 1).padStart(2, "0");
  const dia = String(manana.getDate()).padStart(2, "0");


  fecha.min = `${anio}-${mes}-${dia}`;

  // Evita escribir la fecha con el teclado
  fecha.addEventListener("keydown", (e) => {
    e.preventDefault();
  });

  // Evita pegar texto
  fecha.addEventListener("paste", (e) => {
    e.preventDefault();
  });

  // Evita soltar texto arrastrándolo
  fecha.addEventListener("drop", (e) => {
    e.preventDefault();
  });
}

function inicializarPagina() {
  actualizarEstadoBotonConfirmar();
  configurarFechaRetiro();
}


document.addEventListener("DOMContentLoaded", inicializarPagina);