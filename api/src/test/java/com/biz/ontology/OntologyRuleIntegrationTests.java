package com.biz.ontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev-h2")
@DirtiesContext
class OntologyRuleIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateParentDomainAndGroupIndependentDomains() throws Exception {
        JsonNode procurement = postJson("/api/v1/domains", Map.of(
                "name", "采购业务",
                "code", "GROUP_PROCUREMENT",
                "status", "ENABLED",
                "sortOrder", 10
        ));
        JsonNode sales = postJson("/api/v1/domains", Map.of(
                "name", "销售业务",
                "code", "GROUP_SALES",
                "status", "ENABLED",
                "sortOrder", 20
        ));
        long procurementId = procurement.path("data").path("id").asLong();
        long salesId = sales.path("data").path("id").asLong();

        JsonNode parent = postJson("/api/v1/domains/parents", Map.of(
                "name", "供应链业务",
                "code", "GROUP_SUPPLY_CHAIN",
                "status", "ENABLED",
                "sortOrder", 1,
                "childDomainIds", List.of(procurementId, salesId)
        ));
        long parentId = parent.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/domains/{id}", procurementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentId").value(parentId));
        mockMvc.perform(get("/api/v1/domains/{id}", salesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentId").value(parentId));
    }

    @Test
    void shouldListOntologiesWithoutAnyDomainAsUnclassified() throws Exception {
        postJson("/api/v1/ontologies", Map.of(
                "name", "未归类本体",
                "code", "UNCLASSIFIED_ONTOLOGY",
                "status", "ENABLED",
                "domainIds", List.of()
        ));

        MvcResult result = mockMvc.perform(get("/api/v1/ontologies")
                        .param("unclassified", "true")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .path("data").path("items");
        boolean found = false;
        for (JsonNode item : items) {
            if ("UNCLASSIFIED_ONTOLOGY".equals(item.path("code").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void parentDomainShouldIncludeOntologiesFromAllChildDomains() throws Exception {
        JsonNode childA = postJson("/api/v1/domains", Map.of(
                "name", "父领域汇总子领域A",
                "code", "PARENT_SCOPE_CHILD_A",
                "status", "ENABLED",
                "sortOrder", 10
        ));
        JsonNode childB = postJson("/api/v1/domains", Map.of(
                "name", "父领域汇总子领域B",
                "code", "PARENT_SCOPE_CHILD_B",
                "status", "ENABLED",
                "sortOrder", 20
        ));
        long childAId = childA.path("data").path("id").asLong();
        long childBId = childB.path("data").path("id").asLong();

        JsonNode parent = postJson("/api/v1/domains/parents", Map.of(
                "name", "父领域汇总测试",
                "code", "PARENT_SCOPE",
                "status", "ENABLED",
                "sortOrder", 1,
                "childDomainIds", List.of(childAId, childBId)
        ));
        long parentId = parent.path("data").path("id").asLong();

        postJson("/api/v1/ontologies", Map.of(
                "name", "子领域A本体",
                "code", "PARENT_SCOPE_ONTOLOGY_A",
                "status", "ENABLED",
                "domainIds", List.of(childAId)
        ));
        postJson("/api/v1/ontologies", Map.of(
                "name", "子领域B本体",
                "code", "PARENT_SCOPE_ONTOLOGY_B",
                "status", "ENABLED",
                "domainIds", List.of(childBId)
        ));

        mockMvc.perform(get("/api/v1/ontologies")
                        .param("domainId", String.valueOf(parentId))
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.code == 'PARENT_SCOPE_ONTOLOGY_A')]").exists())
                .andExpect(jsonPath("$.data.items[?(@.code == 'PARENT_SCOPE_ONTOLOGY_B')]").exists());

        mockMvc.perform(get("/api/v1/ontologies")
                        .param("domainId", String.valueOf(childAId))
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.code == 'PARENT_SCOPE_ONTOLOGY_A')]").exists())
                .andExpect(jsonPath("$.data.items[?(@.code == 'PARENT_SCOPE_ONTOLOGY_B')]").doesNotExist());
    }

    @Test
    void fullOntologyAndRuleLifecycleShouldMatchDetailedDesign() throws Exception {
        JsonNode rootDomain = postJson("/api/v1/domains", Map.of(
                "name", "农贸业务",
                "code", "AGRI_TRADE",
                "description", "一期验收领域",
                "status", "ENABLED",
                "sortOrder", 10
        ));
        long rootDomainId = rootDomain.path("data").path("id").asLong();

        JsonNode childDomain = postJson("/api/v1/domains", Map.of(
                "parentId", rootDomainId,
                "name", "采购管理",
                "code", "PROCUREMENT",
                "status", "ENABLED",
                "sortOrder", 10
        ));
        assertThat(childDomain.path("code").asText()).isEqualTo("SUCCESS");

        MvcResult domainTreeResult = mockMvc.perform(get("/api/v1/domains/tree"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode domainTree = objectMapper.readTree(domainTreeResult.getResponse().getContentAsByteArray()).path("data");
        JsonNode agriTradeDomain = null;
        for (JsonNode node : domainTree) {
            if ("AGRI_TRADE".equals(node.path("code").asText())) {
                agriTradeDomain = node;
                break;
            }
        }
        assertThat(agriTradeDomain).isNotNull();
        assertThat(agriTradeDomain.path("children").get(0).path("code").asText()).isEqualTo("PROCUREMENT");

        JsonNode orderOntology = postJson("/api/v1/ontologies", Map.of(
                "name", "采购订单",
                "code", "PURCHASE_ORDER",
                "description", "采购订单本体",
                "status", "ENABLED",
                "domainIds", List.of(rootDomainId)
        ));
        long orderOntologyId = orderOntology.path("data").path("id").asLong();

        JsonNode supplierOntology = postJson("/api/v1/ontologies", Map.of(
                "name", "供应商",
                "code", "SUPPLIER",
                "status", "ENABLED",
                "domainIds", List.of(rootDomainId)
        ));
        long supplierOntologyId = supplierOntology.path("data").path("id").asLong();

        JsonNode orderNo = postJson("/api/v1/ontologies/" + orderOntologyId + "/properties", Map.of(
                "name", "订单编号",
                "code", "ORDER_NO",
                "dataType", "STRING",
                "length", 64,
                "required", true,
                "unique", true,
                "sortOrder", 10,
                "status", "ENABLED"
        ));
        assertThat(orderNo.path("data").path("uniqueFlag").asBoolean()).isTrue();

        JsonNode amount = postJson("/api/v1/ontologies/" + orderOntologyId + "/properties", Map.of(
                "name", "订单金额",
                "code", "ORDER_AMOUNT",
                "dataType", "DECIMAL",
                "precision", 18,
                "scale", 2,
                "required", true,
                "unique", false,
                "sortOrder", 20,
                "status", "ENABLED"
        ));
        long amountPropertyId = amount.path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/ontologies/{id}/properties", orderOntologyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "第二业务主键",
                                "code", "SECOND_KEY",
                                "dataType", "STRING",
                                "unique", true
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROPERTY_UNIQUE_CONFLICT"));
        //这里修改了一下，原代码为.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("PROPERTY_UNIQUE_CONFLICT")));

        JsonNode orderSupplierNo = postJson("/api/v1/ontologies/" + orderOntologyId + "/properties", Map.of(
                "name", "供应商编号",
                "code", "SUPPLIER_NO",
                "dataType", "STRING",
                "length", 64,
                "required", true,
                "unique", false,
                "sortOrder", 30,
                "status", "ENABLED"
        ));
        long orderSupplierPropertyId = orderSupplierNo.path("data").path("id").asLong();

        JsonNode supplierNo = postJson("/api/v1/ontologies/" + supplierOntologyId + "/properties", Map.of(
                "name", "供应商编号",
                "code", "SUPPLIER_NO",
                "dataType", "STRING",
                "length", 64,
                "required", true,
                "unique", true,
                "sortOrder", 10,
                "status", "ENABLED"
        ));
        long supplierPropertyId = supplierNo.path("data").path("id").asLong();

        postJson("/api/v1/ontologies/" + orderOntologyId + "/relations", Map.of(
                "name", "关联供应商",
                "code", "HAS_SUPPLIER",
                "targetOntologyId", supplierOntologyId,
                "cardinality", "MANY_TO_ONE",
                "sourcePropertyId", orderSupplierPropertyId,
                "targetPropertyId", supplierPropertyId,
                "status", "ENABLED"
        ));

        mockMvc.perform(get("/api/v1/ontology-graph").param("domainId", String.valueOf(rootDomainId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes.length()").value(2))
                .andExpect(jsonPath("$.data.edges[0].code").value("HAS_SUPPLIER"));

        JsonNode rule = postJson("/api/v1/rules", Map.of(
                "name", "大额订单检查",
                "code", "RULE_ORDER_AMOUNT_GT_200000",
                "ontologyId", orderOntologyId,
                "description", "采购订单金额超过阈值时返回大额订单结果",
                "enabled", true,
                "condition", Map.of(
                        "propertyId", amountPropertyId,
                        "operator", "GT",
                        "compareValue", 200000
                ),
                "action", Map.of(
                        "resultCode", "HIGH_AMOUNT",
                        "resultName", "大额订单",
                        "message", "订单金额超过 200000 元"
                ),
                "changeNote", "初始版本"
        ));
        long ruleId = rule.path("data").path("id").asLong();
        long ruleDefinitionVersion = rule.path("data").path("version").asLong();
        long v1Id = rule.path("data").path("currentVersionId").asLong();
        assertThat(rule.path("data").path("currentVersionNo").asInt()).isEqualTo(1);

        mockMvc.perform(post("/api/v1/rules/{id}/test", ruleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "versionId", v1Id,
                                "values", Map.of(String.valueOf(amountPropertyId), 300000)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matched").value(true))
                .andExpect(jsonPath("$.data.action.resultCode").value("HIGH_AMOUNT"));

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "错误操作符规则",
                                "code", "INVALID_AMOUNT_CONTAINS",
                                "ontologyId", orderOntologyId,
                                "condition", Map.of(
                                        "propertyId", amountPropertyId,
                                        "operator", "CONTAINS",
                                        "compareValue", "2"
                                ),
                                "action", Map.of("resultCode", "INVALID", "resultName", "错误")
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RULE_OPERATOR_NOT_SUPPORTED"));
                //.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("RULE_OPERATOR_NOT_SUPPORTED")));

        JsonNode updatedRule = putJson("/api/v1/rules/" + ruleId, Map.of(
                "name", "大额订单检查",
                "code", "RULE_ORDER_AMOUNT_GT_200000",
                "description", "阈值调整为300000",
                "condition", Map.of(
                        "propertyId", amountPropertyId,
                        "operator", "GT",
                        "compareValue", 300000
                ),
                "action", Map.of(
                        "resultCode", "HIGH_AMOUNT",
                        "resultName", "大额订单",
                        "message", "订单金额超过 300000 元"
                ),
                "changeNote", "调整阈值",
                "version", ruleDefinitionVersion
        ));
        assertThat(updatedRule.path("data").path("currentVersionNo").asInt()).isEqualTo(2);

        mockMvc.perform(get("/api/v1/rules/{id}/versions", ruleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].versionNo").value(2))
                .andExpect(jsonPath("$.data[1].versionNo").value(1));

        long updatedDefinitionVersion = updatedRule.path("data").path("version").asLong();
        mockMvc.perform(post("/api/v1/rules/{ruleId}/versions/{versionId}/switch", ruleId, v1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("version", updatedDefinitionVersion))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentVersionNo").value(1));

        mockMvc.perform(delete("/api/v1/ontologies/{ontologyId}/properties/{propertyId}",
                        orderOntologyId, amountPropertyId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROPERTY_REFERENCED"));
                //.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("PROPERTY_REFERENCED")));

        long ontologyVersion = orderOntology.path("data").path("version").asLong();
        mockMvc.perform(patch("/api/v1/ontologies/{id}/status", orderOntologyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "DISABLED",
                                "version", ontologyVersion
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "禁用本体规则",
                                "code", "DISABLED_ONTOLOGY_RULE",
                                "ontologyId", orderOntologyId,
                                "condition", Map.of(
                                        "propertyId", amountPropertyId,
                                        "operator", "GT",
                                        "compareValue", 1
                                ),
                                "action", Map.of("resultCode", "X", "resultName", "X")
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RULE_ONTOLOGY_DISABLED"));
                //.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("RULE_ONTOLOGY_DISABLED")));
    }

    private JsonNode postJson(String path, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private JsonNode putJson(String path, Object body) throws Exception {
        MvcResult result = mockMvc.perform(put(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
