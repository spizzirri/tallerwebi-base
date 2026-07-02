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
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: "productoId=" + productoId
  }).then(() => {
    producto.remove();
    actualizarEstadoBotonConfirmar();
  });
}

function actualizarEstadoBotonConfirmar() {
  const btnConfirmar = document.getElementById("btnConfirmarPedido");
  if (!btnConfirmar) return;
  btnConfirmar.disabled = false;
}

function configurarFechaRetiro() {
  const fecha = document.getElementById("fechaRetiro");
  if (!fecha) return;

  const manana = new Date();
  manana.setDate(manana.getDate() + 1);

  const anio = manana.getFullYear();
  const mes = String(manana.getMonth() + 1).padStart(2, "0");
  const dia = String(manana.getDate()).padStart(2, "0");

  fecha.min = `${anio}-${mes}-${dia}`;

  fecha.addEventListener("keydown", (e) => e.preventDefault());
  fecha.addEventListener("paste", (e) => e.preventDefault());
  fecha.addEventListener("drop", (e) => e.preventDefault());
}

function configurarEnvioFormulario() {
  const form = document.getElementById("formDistribucion");
  if (!form) return;

  form.addEventListener("submit", function (e) {
    const cantidades = document.querySelectorAll(".qty-input");
    const hayAlgunaCantidad = Array.from(cantidades)
      .some((input) => parseInt(input.value) > 0);

    if (!hayAlgunaCantidad) {
      e.preventDefault();

      const alertasViejas = document.querySelectorAll(".alerta-mensaje");
      alertasViejas.forEach(alerta => alerta.remove());

      const nuevaAlerta = document.createElement("div");
      nuevaAlerta.className = "alerta-mensaje alerta-error d-flex align-items-center gap-2";
      nuevaAlerta.innerHTML = `
    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" width="20" height="20">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
    </svg>
    <span>Debe asignar al menos una unidad de algún producto a sus hijos antes de confirmar.</span>
  `;

      const banner = document.querySelector(".banner-titulos");
      if (banner) {
        banner.insertAdjacentElement("afterend", nuevaAlerta);
      }

      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  });
}

function guardarYVolverAlHome() {
  const form = document.getElementById("formDistribucion");
  if (!form) {
    window.location.href = "/spring/home";
    return;
  }

  // Enviamos los datos usando URLSearchParams filtrando solo los inputs de cantidades
  const params = new URLSearchParams();
  const inputs = form.querySelectorAll(".qty-input");

  inputs.forEach(input => {
    params.append(input.name, input.value);
  });

  fetch("/spring/distribucion/guardar-borrador", {
    method: "POST",
    body: params
  })
    .then(() => {
      window.location.href = "/spring/home";
    })
    .catch((err) => {
      console.error("Error al guardar el borrador:", err);
      window.location.href = "/spring/home";
    });
}

function inicializarPagina() {
  actualizarEstadoBotonConfirmar();
  configurarFechaRetiro();
  configurarEnvioFormulario();
}

document.addEventListener("DOMContentLoaded", inicializarPagina);