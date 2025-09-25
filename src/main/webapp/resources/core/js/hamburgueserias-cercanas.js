import {
  renderizarHamburgueserias,
  renderizarError,
} from "./hamburgueserias-cercanas_funciones.js";

document.addEventListener("DOMContentLoaded", async function () {
  try {
    await renderizarHamburgueserias();
    // Hide loader when data is successfully loaded
    if (window.LoaderUtils) {
      window.LoaderUtils.hide();
    }
  } catch (error) {
    console.error(error);
    renderizarError(
      error.message || "Error al cargar hamburgueserías cercanas."
    );
    // Hide loader even if there's an error
    if (window.LoaderUtils) {
      window.LoaderUtils.hide();
    }
  }
});
