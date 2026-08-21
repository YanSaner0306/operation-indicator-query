/**
 * 模块6测试：通过REST闭环验证数据源创建、分页、空密码更新、启停、软删除及密码不出参。
 * 技术栈：Spring Boot Test、MockMvc、Spring Security Test、H2/Flyway与JPA。
 */
package com.biz.ontology.data;

import com.biz.ontology.data.repository.DataSourceConfigRepository;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev-h2") @DirtiesContext
@WithMockUser(authorities={"DATASOURCE_VIEW","DATASOURCE_MANAGE"})
class DataSourceCrudIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ObjectMapper mapper; @Autowired DataSourceConfigRepository repository;
    @Test void shouldCompleteCrudWithoutExposingOrOverwritingPassword() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/data-sources").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of(
                "name","测试数据源","dbType","MYSQL","host","db.example.test","port",3306,"databaseName","orders","username","reader","password","Secret123!"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.password").doesNotExist()).andExpect(jsonPath("$.data.passwordCipher").doesNotExist()).andReturn();
        JsonNode data=mapper.readTree(created.getResponse().getContentAsByteArray()).path("data"); long id=data.path("id").asLong(); long version=data.path("version").asLong();
        String cipher=repository.findByIdAndDeletedFlagFalse(id).orElseThrow().getPasswordCipher();
        MvcResult updated=mockMvc.perform(put("/api/v1/data-sources/{id}",id).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of(
                "name","测试数据源更新","dbType","MYSQL","host","db.example.test","port",3306,"databaseName","orders","username","reader","password","","version",version))))
                .andExpect(status().isOk()).andReturn();
        assertThat(repository.findByIdAndDeletedFlagFalse(id).orElseThrow().getPasswordCipher()).isEqualTo(cipher);
        long nextVersion=mapper.readTree(updated.getResponse().getContentAsByteArray()).path("data").path("version").asLong();
        mockMvc.perform(patch("/api/v1/data-sources/{id}/enabled",id).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status","ENABLED","version",nextVersion)))).andExpect(status().isOk());
        long deleteVersion=repository.findByIdAndDeletedFlagFalse(id).orElseThrow().getVersion();
        mockMvc.perform(delete("/api/v1/data-sources/{id}",id).param("version",String.valueOf(deleteVersion))).andExpect(status().isOk());
        assertThat(repository.findByIdAndDeletedFlagFalse(id)).isEmpty();
    }
}
