package kr.hhplus.be.server.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InventoryDeductedEvent extends BaseEvent {
    private Long orderId;
    private List<InventoryItem> inventoryItems;

    @JsonCreator
    public InventoryDeductedEvent(
            @JsonProperty("orderId") Long orderId,
            @JsonProperty("inventoryItems") List<InventoryItem> inventoryItems) {
        super("INVENTORY_DEDUCTED", "ecommerce-inventory-service");
        this.orderId = orderId;
        this.inventoryItems = inventoryItems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class InventoryItem {
        private Long productId;
        private Integer deductedQuantity;
        private Integer remainingStock;

        @JsonCreator
        public InventoryItem(
                @JsonProperty("productId") Long productId,
                @JsonProperty("deductedQuantity") Integer deductedQuantity,
                @JsonProperty("remainingStock") Integer remainingStock) {
            this.productId = productId;
            this.deductedQuantity = deductedQuantity;
            this.remainingStock = remainingStock;
        }
    }
}
