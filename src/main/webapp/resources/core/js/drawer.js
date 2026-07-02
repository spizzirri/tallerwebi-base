/* eslint-disable no-unused-vars */
document.addEventListener("DOMContentLoaded", function() {
  const btnHamburguesa = document.getElementById("btnHamburguesa");
  const panelCliente = document.getElementById("panelCliente");
  const panelKiosquero = document.getElementById("panelKiosquero");
  const drawerOverlay = document.getElementById("drawerOverlay");

  if (btnHamburguesa) {
    btnHamburguesa.addEventListener("click", function() {
      if (panelCliente) panelCliente.classList.add("drawer-open");
      if (panelKiosquero) panelKiosquero.classList.add("drawer-open");
      if (drawerOverlay) drawerOverlay.classList.add("active");
    });
  }

  if (drawerOverlay) {
    drawerOverlay.addEventListener("click", function() {
      if (panelCliente) panelCliente.classList.remove("drawer-open");
      if (panelKiosquero) panelKiosquero.classList.remove("drawer-open");
      if (drawerOverlay) drawerOverlay.classList.remove("active");
    });
  }
});

function cerrarDrawer() {
  const panelCliente = document.getElementById("panelCliente");
  const panelKiosquero = document.getElementById("panelKiosquero");
  const drawerOverlay = document.getElementById("drawerOverlay");

  if (panelCliente) panelCliente.classList.remove("drawer-open");
  if (panelKiosquero) panelKiosquero.classList.remove("drawer-open");
  if (drawerOverlay) drawerOverlay.classList.remove("active");
}