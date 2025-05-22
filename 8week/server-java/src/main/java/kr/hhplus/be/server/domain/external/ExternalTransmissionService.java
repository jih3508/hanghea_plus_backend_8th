package kr.hhplus.be.server.domain.external;

import kr.hhplus.be.server.domain.order.model.DomainOrder;
import org.springframework.stereotype.Service;

@Service
public class ExternalTransmissionService {

    public Boolean sendOrderData(DomainOrder order) {
        return true;
    }
}
