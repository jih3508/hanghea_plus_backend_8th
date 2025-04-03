package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.response.ProductResponse;
import kr.hhplus.be.server.product.response.ProductTopResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping("/productId")
    public ResponseEntity<ProductResponse> getProduct(){
        return ResponseEntity.ok(new ProductResponse());
    }

    @GetMapping("/top")
    public ResponseEntity<List<ProductTopResponse>> getTop(){
        return ResponseEntity.ok(new ArrayList<ProductTopResponse>());
    }
}
