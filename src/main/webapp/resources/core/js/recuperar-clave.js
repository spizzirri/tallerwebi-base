const paso1 = document.getElementById("paso1");
const paso2 = document.getElementById("paso2");
const paso3 = document.getElementById("paso3");
const paso4 = document.getElementById("paso4");

const emailInput = document.getElementById("email-Rec");
const codigoInput = document.getElementById("codigo");
const nuevaClaveInput = document.getElementById("nuevaClave");

const btnEnviar = document.getElementById("btn-enviarCod");
const btnVerificar = document.getElementById("btn-verificarCod");
const btnCambiar = document.getElementById("btn-cambiarClave");

emailInput.addEventListener("keyup", () => {
  btnEnviar.disabled = emailInput.value.length === 0;
});

btnEnviar.addEventListener("click", async () => {

  btnEnviar.disabled = true;

  const params = new URLSearchParams();
  params.append("email", emailInput.value);

  const res = await fetch("/spring/verificar-email", {
    method: "POST",
    body: params
  });

  if (res.ok) {
    /*console.log("Codigo enviado: 1234");*/
    paso1.classList.add("oculto");
    paso2.classList.remove("oculto");
  } else {
    alert("El email no esta registrado");
    btnEnviar.disabled = false;
  }
});

codigoInput.addEventListener("input", () => {
  btnVerificar.disabled = codigoInput.value.length === 0;
});

/*
btnVerificar.addEventListener("click", () => {
  if (codigoInput.value === "1234") {
    paso2.classList.add("oculto");
    paso3.classList.remove("oculto");
  } else {
    alert("Codigo incorrecto");
  }
});
*/

btnVerificar.addEventListener("click", async () => {

  const params = new URLSearchParams();
  params.append("codigo", codigoInput.value);

  const res = await fetch("/spring/verificar-codigo", {
    method: "POST",
    body: params
  });

  if (res.ok) {
    paso2.classList.add("oculto");
    paso3.classList.remove("oculto");
  } else {
    alert("Código incorrecto o vencido");
  }

});

nuevaClaveInput.addEventListener("keyup", () => {
  btnCambiar.disabled = nuevaClaveInput.value.length === 0;
});

btnCambiar.addEventListener("click", async () => {
  const params = new URLSearchParams();
  params.append("email", emailInput.value);
  params.append("nuevaClave", nuevaClaveInput.value);

  const res = await fetch("/spring/actualizar-contrasenia", {
    method: "POST",
    body: params
  });

  if (res.ok) {
    paso3.classList.add("oculto");
    paso4.classList.remove("oculto");
  } else {
    alert("No fue posible cambiar la contraseña.");
  }
});
