package kr.hhplus.be.server.infrastructure.product.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum ProductCategory {

    FOOD("FOOD", "음식"),
    ELECTRONIC_DEVICES("ELECTRONIC_DEVICES", "전자기기"),
    ETC("ETC", "기타");


    private final String code;

    private final String description;

    ProductCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ProductCategory fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            log.warn("ProductCategory code is null or empty, returning ETC");
            return ETC;
        }

        for (ProductCategory category : ProductCategory.values()) {
            if (category.code.equalsIgnoreCase(code.trim())) {
                return category;
            }
        }

        log.warn("Unknown ProductCategory code: {}, returning ETC", code);
        return ETC; // 기본값으로 ETC 반환
    }
}
