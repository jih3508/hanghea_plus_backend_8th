package kr.hhplus.be.server.interfaces.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.interfaces.api.coupon.CouponController;
import kr.hhplus.be.server.interfaces.api.order.OrderController;
import kr.hhplus.be.server.interfaces.api.point.PointController;
import kr.hhplus.be.server.interfaces.api.product.ProductController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        PointController.class,
        ProductController.class,
        OrderController.class,
        CouponController.class,
})
public abstract class ControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @MockitoBean
    private PointFacade pointFacade;

    @MockitoBean
    private ProductFacade productFacade;

    @MockitoBean
    private CouponFacade couponFacade;


}
