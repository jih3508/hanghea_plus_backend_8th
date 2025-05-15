package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.util.RedisKeysPrefix;
import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 랭킹 통합 테스트")
class ProductRankServiceIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ProductRankServiceIntegrationTest.class);

    @Autowired
    private ProductRankService service;

    @Autowired
    private ProductRankRepository repository;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    private String redisKey = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX;

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");


    @AfterEach
    void setUp(){
        redisTemplate.delete(redisKey);
    }


    @Test
    @DisplayName("상품 이력 저장 테스트")
    void 상품_이력_저장(){
        //given
        CreateProductRank rank1 = new CreateProductRank(1L, 1, 1000);
        CreateProductRank rank2 = new CreateProductRank(2L, 2, 500);
        CreateProductRank rank3 = new CreateProductRank(3L, 3, 200);
        CreateProductRank rank4 = new CreateProductRank(4L, 4, 100);
        List<CreateProductRank> ranks = List.of(rank1, rank2, rank3, rank4);

        //when
        service.save(ranks);
        List<DomainProductRank> result = repository.findAllProductRank();

        //then
        assertThat(result).hasSize(4);


    }

    @Test
    @DisplayName("오늘 상품 이력 조회 테스트")
    void 상품_조회_테스트(){
        //given
        CreateProductRank rank1 = new CreateProductRank(1L, 1, 1000);
        CreateProductRank rank2 = new CreateProductRank(2L, 2, 500);
        CreateProductRank rank3 = new CreateProductRank(3L, 3, 200);
        CreateProductRank rank4 = new CreateProductRank(4L, 4, 100);
        List<CreateProductRank> ranks = List.of(rank1, rank2, rank3, rank4);
        repository.manySave(ranks);

        // when
        List<DomainProductRank> result = service.todayProductRank();

        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("최근 3일 주문 데이터 가져오기 테스트")
    void 레디스_최근_3일(){
        //given
        LocalDate today = LocalDate.now();
        String yesterday = today.minusDays(1).format(DATE_FORMATTER);

        for (long i = 1; i < 10; i++) {
            long score = (long) Math.random() * 10000 + 10L;
            redisTemplate.opsForZSet().add(redisKey + yesterday, i, score);
        }


        // when
        List<DomainProductRank> result = service.todayProductRank();

        // then
        assertThat(result).hasSize(5);

    }
}