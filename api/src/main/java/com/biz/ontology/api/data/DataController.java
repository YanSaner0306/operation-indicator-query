package com.biz.ontology.api.data;

import com.biz.ontology.api.common.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "data", description = "data 占位接口")
@RestController
@RequestMapping("/api/v1/data")
public class DataController {

    @GetMapping("/ping")
    public R<Map<String, String>> ping() {
        return R.ok(Map.of("module", "data", "status", "ready"));
    }
}
