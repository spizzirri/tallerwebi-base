export function validarCamposDeLogin(inputEmailValue, inputPasswordValue) {
    return inputEmailValue?.length > 0 && inputPasswordValue?.length > 0;
  }