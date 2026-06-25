package com.tallerwebi.dominio.ServicioImagenesTest;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.tallerwebi.dominio.SubidaDeImgs.ServicioImagenesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ServicioImagenesTest {
    private Uploader uploaderMock;
    private MultipartFile archivoMock;
    private ServicioImagenesImpl servicioImagenes;

    @BeforeEach
    public void init() {
        Cloudinary cloudinaryMock = mock(Cloudinary.class);
        uploaderMock = mock(Uploader.class);
        archivoMock = mock(MultipartFile.class);
        //NUESTRO CODIGO HACE UN ENCADENAMIENTO, mockeamos ese encadenamiento
        when(cloudinaryMock.uploader()).thenReturn(uploaderMock);

        servicioImagenes = new ServicioImagenesImpl(cloudinaryMock);
    }

    @Test
    public void subirImagenNormalDelUsuarioDeberiaRetornarUrlSegura() throws IOException {
        byte[] contenidoFalso = "imagen-falsa".getBytes();
        when(archivoMock.getBytes()).thenReturn(contenidoFalso);

        // Simulamos la respuesta que daría Cloudinary en un mapa
        Map<String, Object> resultadoSimulado = new HashMap<>();
        resultadoSimulado.put("secure_url", "https://res.cloudinary.com/kionet/image/upload/foto.jpg");

        // any(Map.class) porque ObjectUtils.asMap devuelve un mapa de configuración
        when(uploaderMock.upload(eq(contenidoFalso), any(Map.class))).thenReturn(resultadoSimulado);

        String urlObtenida = servicioImagenes.subirImagen(archivoMock, "usuarios");

        assertThat(urlObtenida, equalTo("https://res.cloudinary.com/kionet/image/upload/foto.jpg"));
        verify(uploaderMock, times(1)).upload(eq(contenidoFalso), any(Map.class));

    }

    @Test
    public void subirImagenHijoDeberiaAplicarTransformacionYRetornarUrl() throws IOException {
        byte[] contenidoFalso = "imagen-carnet".getBytes();
        when(archivoMock.getBytes()).thenReturn(contenidoFalso);

        Map<String, Object> resultadoSimulado = new HashMap<>();
        resultadoSimulado.put("secure_url", "https://res.cloudinary.com/kionet/image/upload/carnet_hijo.jpg");

        when(uploaderMock.upload(eq(contenidoFalso), any(Map.class))).thenReturn(resultadoSimulado);

        //ACA EMPEZAMOS CON EL TESTEO DE LA TRANSFORMACION DE LA IMAGEN CON ArgumentCaptor
        // Creamos el capturador para atrapar el mapa de opciones
        ArgumentCaptor<Map<String, Object>> capturadorDeMapa = ArgumentCaptor.forClass(Map.class);

        // 2. Ejecución
        String urlObtenida = servicioImagenes.subirImagenHijo(archivoMock, "hijos");

        // 3. Verificaciones
        assertThat(urlObtenida, equalTo("https://res.cloudinary.com/kionet/image/upload/carnet_hijo.jpg"));

        // Verificamos el metodo pero ATRAPAMOS el mapa que se usó internamente
        verify(uploaderMock, times(1)).upload(eq(contenidoFalso), capturadorDeMapa.capture());

        // Recuperamos el mapa real que ejecutó tu código
        Map <String, Object>mapaEjecutado = capturadorDeMapa.getValue();

        // 4. ¡Acá testeamos las transformaciones de Cloudinary!
        assertThat(mapaEjecutado.get("folder"), equalTo("hijos"));

        // Verificamos que exista la clave "transformation"
        assertThat(mapaEjecutado.containsKey("transformation"), equalTo(true));

        // Convertimos la transformación a String para verificar que tenga tus reglas de carnet
        String transformacionString = mapaEjecutado.get("transformation").toString();

        // Corregido: Buscamos las siglas nativas de Cloudinary
        assertThat(transformacionString.contains("width_400") || transformacionString.contains("w_400"), equalTo(true));
        assertThat(transformacionString.contains("height_400") || transformacionString.contains("h_400"), equalTo(true));
        assertThat(transformacionString.contains("crop_thumb") || transformacionString.contains("c_thumb"), equalTo(true));
        assertThat(transformacionString.contains("gravity_face") || transformacionString.contains("g_face"), equalTo(true));
    }

    @Test
    public void siCloudinaryFallaDeberiaLanzarRuntimeException() throws IOException {
        when(archivoMock.getBytes()).thenReturn("datos".getBytes());

        // Forzamos el error de lectura/escritura de Cloudinary
        doThrow(IOException.class).when(uploaderMock).upload(any(byte[].class), any(Map.class));

        assertThrows(RuntimeException.class, () -> servicioImagenes.subirImagen(archivoMock, "carpeta"));
    }

}
