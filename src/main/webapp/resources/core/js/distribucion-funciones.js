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
document.addEventListener("DOMContentLoaded", actualizarEstadoBotonConfirmar);