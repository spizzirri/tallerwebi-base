import { validarCamposDeLogin } from "./login_funciones.js";

const btnLoginNode = document.getElementById("btn-login");
const inputEmailNode = document.getElementById("email");
const inputPasswordNode = document.getElementById("password");

inputEmailNode.addEventListener("keyup", (event) => {
  const inputEmailValue = event.target.value;
  const inputPasswordValue = inputPasswordNode.value;
  btnLoginNode.disabled = !validarCamposDeLogin(inputEmailValue, inputPasswordValue);
});

inputPasswordNode.addEventListener("keyup", (event) => {
  const inputEmailValue = inputEmailNode.value;
  const inputPasswordValue = event.target.value;
  btnLoginNode.disabled = !validarCamposDeLogin(inputEmailValue, inputPasswordValue);
});