package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.entity.ProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRankService {

    private final ProductRankRepository repository;

    public void save(List<ProductRank> productRank){
        repository.manySave(productRank);
    }

    public List<ProductRank> todayProductRank(){
        return repository.todayProductRank();
    }

}
