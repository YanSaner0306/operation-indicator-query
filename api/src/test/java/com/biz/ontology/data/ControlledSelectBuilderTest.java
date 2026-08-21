/**
 * 模块11-13测试：受控查询 SQL 与参数计划。
 * 功能：验证标识符引用、AND 条件、IN 占位符、业务键和 LIMIT，并确认业务值不会拼入 SQL。
 * 技术栈：JUnit 5、AssertJ 与纯 Java 单元测试。
 */
package com.biz.ontology.data;

import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.binding.query.ControlledSelectBuilder;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ControlledSelectBuilderTest {
    @Test void shouldBuildParameterizedSingleTableSelect() {
        OntologyTableBindingEntity binding=new OntologyTableBindingEntity(); binding.setTableName("orders");
        OntologyFieldBindingEntity id=mapping("order_id"), amount=mapping("amount");
        BindingFilterConditionEntity status=filter("status","VARCHAR",BindingFilterOperator.EQ,"PAID' OR 1=1 --");
        BindingFilterConditionEntity levels=filter("level","INTEGER",BindingFilterOperator.IN,"1,2,3");
        var plan=new ControlledSelectBuilder().build("`",binding,List.of(id,amount),List.of(status,levels),"order_id",99L,1);
        assertThat(plan.sql()).isEqualTo("SELECT `order_id`,`amount` FROM `orders` WHERE `status` = ? AND `level` IN (?,?,?) AND `order_id` = ? LIMIT 1");
        assertThat(plan.sql()).doesNotContain("PAID' OR 1=1 --");
        assertThat(plan.parameters()).containsExactly("PAID' OR 1=1 --",1L,2L,3L,99L);
    }
    private OntologyFieldBindingEntity mapping(String name){var v=new OntologyFieldBindingEntity();v.setSourceColumn(name);return v;}
    private BindingFilterConditionEntity filter(String column,String type,BindingFilterOperator operator,String value){var v=new BindingFilterConditionEntity();v.setSourceColumn(column);v.setSourceDataType(type);v.setOperator(operator);v.setTypedValue(value);return v;}
}
