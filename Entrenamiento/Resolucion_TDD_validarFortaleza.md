# Ejemplo de como resolver con TDD el ejercicio Validador de Fortaleza de Contraseña

## Por donde arrancar?
Una forma de pensar como arrancar a resolver un ejercicio con TDD es tomar alguna de las respuestas que ocurre en los extremos de las validaciones. Comunmente tomamos la mas sencilla de reproducir, para luego comenzar a iterar.


Para este ejercicio comenzaremos con la validacion de "INVALIDA". Lo primero que hacemos es escribir la prueba unitaria automatizada que valide ese caso.
> Es una cadena vacía o nula.

## Iteracion 1. 

```java
@Test
public void deberDevolverINVALIDACuandoLaContraseñaEsNula(){

    String contraseñaVacia = null;
    String fortaleza = TDDEjercicio.validarFortaleza(contraseñaVacia);
    assertThat(fortaleza, equalToIgnoringCase("INVALIDA"));
}

@Test
public void deberDevolverINVALIDACuandoLaContraseñaEsVacia(){

    String contraseñaVacia = "";
    String fortaleza = TDDEjercicio.validarFortaleza(contraseñaVacia);
    assertThat(fortaleza, equalToIgnoringCase("INVALIDA"));
}
```
Una vez que ya tenemos las pruebas escritas para los casos que queremos probar, vamos a escribir el codigo productivo **minimo y necesario** para que pasen las pruebas.

```java
public class TDDEjercicio{

    public static String validarFortaleza(String contraseña){
        return "INVALIDA";
    }
}
```

Como vemos, el codigo lo hemos hecho super simple, lo que buscamos es que la prueba pase.

En este momento, con las pruebas en verde, nos debemos preguntar si podemos mejorar el codigo productivo para que sea mas performante, legible, si puede modularizar, etc. Es el momento de hacer dado que todas las pruebas estan en verde. De momento, no hay mucho para cambiar pero si lo hubiera solo basta con volver a correr las pruebas para garantizar que nada se ha roto con los cambios implementados.


## Iteracion 2. 

Avanzamos y ahora buscamos resolver la condicion de DEBIL. Escribimos las pruebas
> Entre 1 y  8 caracteres

```java
@Test
public void debeDevolverDEBILCuandoLaContraseñaTieneUnCaracterYEs_A(){

    String contraseñaUnCaracterEjemplo = "A";
    String fortaleza = TDDEjercicio.validarFortaleza(contraseñaUnCaracterEjemplo);
    assertThat(fortaleza, equalToIgnoringCase("DEBIL"));
}

@Test
public void debeDevolverDEBILCuandoLaContraseñaTieneOchoCaracteresYEs_AAAABBBB(){

    String contraseña8CaracteresEjemplo = "AAAABBBB";
    String fortaleza = TDDEjercicio.validarFortaleza(contraseña8CaracteresEjemplo);
    assertThat(fortaleza, equalToIgnoringCase("DEBIL"));
}
```

Una vez que ya tenemos las pruebas escritas para los casos que queremos probar, vamos a escribir el codigo productivo **minimo y necesario** para que pasen las pruebas.

```java
public class TDDEjercicio{

    public static String validarFortaleza(String contraseña){
        Integer cantidadDeCaracteres = contraseña.length();
        if(cantidadDeCaracteres > 0 && cantidadDeCaracteres < 9)
            return "DEBIL";
        return "INVALIDA";
    }
}
```

Como vimos anteriormente, el codigo lo hemos hecho super simple, lo que buscamos es que la prueba pase. Luego de que todas las pruebas pasan nos ponemos a pensar si podemos hacer alguna optimizacion, pero solamente luego de ver que las pruebas dan verde.


## Iteracion 3. 

Avanzamos y ahora buscamos resolver la condicion de MEDIANA. Escribimos las pruebas
> 8 o más caracteres. Contiene 1 número y 1 carácter especial  (-, _, %, $, ?, !, @)

```java
@Test
public void debeDevolverMEDIANACuandoLaContraseñaTieneOchoCaracteresConUnNumeroYUnCaracterEspecial(){

    String contraseñaEjemplo = "AAABBB$1";
    String fortaleza = TDDEjercicio.validarFortaleza(contraseñaEjemplo);
    assertThat(fortaleza, equalToIgnoringCase("MEDIANA"));
}

@Test
public void debeDevolverMEDIANACuandoLaContraseñaTieneNueveCaracteresConUnNumeroYUnCaracterEspecial(){

    String contraseñaEjemplo = "AAABBBC$1";
    String fortaleza = TDDEjercicio.validarFortaleza(contraseñaEjemplo);
    assertThat(fortaleza, equalToIgnoringCase("MEDIANA"));
}

@Test
public void debeDevolverDEBILCuandoLaContraseñaTieneMasDeOchoCaracteresPeroCeroNumerosOCaracteresEspeciales(){

    String contraseñaEjemplo = "AAABBBCCC";
    String fortaleza = TDDEjercicio.validarFortaleza(contraseñaEjemplo);
    assertThat(fortaleza, equalToIgnoringCase("DEBIL"));
}
```

Una vez que ya tenemos las pruebas escritas para los casos que queremos probar, vamos a escribir el codigo productivo **minimo y necesario** para que pasen las pruebas.
En este caso tambien vemos que la nueva validacion nos invita a crear una nueva prueba para el caso de debil

.......

A partir de este punto el alumno puede continuar el ejercicio.

## Que nombre le pongo a las pruebas?

No existe una forma unica de nombrar las pruebas, aunque lo que si debemos asegurar es que los nombres sean de casos bien puntuales. Evitar nombres que se refieran a situaciones genericas, mientras mas especifico mejor. Debemos pensar que escribimos pruebas para que sean leidas a posteriori y que sea facil de comprender su caso a probar.

* debeDevolverDEBILCuandoLaContraseñaTieneUnCaracterYEs_A
* debeDevolverDEBIL__CuandoLaContraseñaTieneUnCaracterYEs_A
* deberiaDevolerDEBIL_SiLaContraseñaTieneUnCaracterYEs_A
* CuandoLaContraseñaTieneUnCaracterYEsA_DebeDevolverDEBIL
* ...

Todas estas variantes de nombres son validad, y seguramente a existan muchas mas, lo importante es ser bien especifico con el valor que se espera y los datos de entrada.