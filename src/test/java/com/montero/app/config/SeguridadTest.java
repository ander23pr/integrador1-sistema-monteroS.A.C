package com.montero.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = {
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password=",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                    "spring.jpa.open-in-view=false"
                })
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rutasPublicas_deberianSerAccesiblesSinAutenticacion() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/registro")).andExpect(status().isOk());
        mockMvc.perform(get("/")).andExpect(status().isFound()); // redirect a /login
    }

    @Test
    void rutaAdmin_sinAutenticacion_deberiaRedirigirALogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isFound());
    }

    @Test
    void rutaUser_sinAutenticacion_deberiaRedirigirALogin() throws Exception {
        mockMvc.perform(get("/viajes")).andExpect(status().isFound());
        mockMvc.perform(get("/reservas/historial")).andExpect(status().isFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rutaAdmin_conRolAdmin_deberiaRetornarOk() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void rutaAdmin_conRolUser_deberiaRetornarForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void rutaUser_conRolUser_deberiaRetornarOk() throws Exception {
        mockMvc.perform(get("/viajes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rutaUser_conRolAdmin_deberiaRetornarForbidden() throws Exception {
        mockMvc.perform(get("/viajes")).andExpect(status().isForbidden());
    }
}
