package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRankService {

    private final ProductRankRepository repository;

    public void save(List<CreateProductRank> productRank){
        repository.manySave(productRank);
    }

    public List<DomainProductRank> todayProductRank(){
        return repository.todayProductRank();
    }

}
