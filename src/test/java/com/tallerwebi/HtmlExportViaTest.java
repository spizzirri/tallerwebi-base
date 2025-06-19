package com.tallerwebi;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class HtmlExportViaTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    @Disabled("Solo para exportar HTML, no es un test funcional")
    public void exportHtml() throws Exception {
        String[][] rutas = {
            {"/home", "test-artifacts/home.html"},
            {"/contacto", "test-artifacts/contact.html"}
        };

        for (String[] ruta : rutas) {
            String contenido = mockMvc.perform(get(ruta[0]))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Files.createDirectories(Path.of("test-artifacts"));
            Files.writeString(Path.of(ruta[1]), contenido);

            System.out.println("✅ Exportado: " + ruta[1]);
        }
    }
}
