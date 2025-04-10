package kr.hhplus.be.server.domain.product.entity;

import lombok.Getter;

@Getter
public enum ProductCategory {

    FOOD("FOOD", "음식"),
    ELECTRONIC_DEVICES("ELECTRONIC_DEVICES", "전자기기"),
    ETC("ECT", "기타");


    private final String code;

    private final String description;

    ProductCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
