package kr.hhplus.be.server.balance.response;


import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BalanceResponse {

    private BigDecimal amount;

    public BalanceResponse(BigDecimal amount) {
        this.amount = amount;
    }

    public static BalanceResponse of(BigDecimal amount) {
        return new BalanceResponse(amount);
    }
}
