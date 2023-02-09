package com.bknote71.BootBatch.model.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.util.List;

import static com.bknote71.BootBatch.model.product.QProduct.product;

@Repository
public class ProductRepositoryImpl implements ProductSearchRepository {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManagerFactory emf) {
        this.queryFactory = new JPAQueryFactory(emf.createEntityManager());
    }

    @Override
    public List<Product> findBySearchCondition(ProductSearchCondition cond) {
        // BooleanBuilder builder = new BooleanBuilder();
        JPAQuery<Product> query = queryFactory.selectFrom(product)
                .where(
                        containName(cond.getName()),
                        priceBetween(cond.getGoePrice(), cond.getLoePrice()),
                        brandnameEq(cond.getBrandname()),
                        categorynameEq(cond.getCategoryname())
                );

        return query.fetch();
    }

    private BooleanExpression containName(String name) {
        return name != null ? product.name.contains(name) : null;
    }

    private BooleanExpression priceBetween(Integer goePrice, Integer loePrice) {
        // Either from or to needs to be non-null;
        // 한쪽은 null 일수는 있다. 이 때 그 한쪽의 범위는 무한대
        // ex) (100, null : 100 ~ 무한), (null ~ 100 : -무한 ~ 100)
        // 둘 다 null 일 때는 <0: null>
        goePrice = (goePrice == null && loePrice == null) ? 0 : goePrice;
        return product.price.between(goePrice, loePrice);
    }

    private BooleanExpression brandnameEq(String brandname) {
        return brandname != null ? product.brandname.contains(brandname) : null;
    }

    private BooleanExpression categorynameEq(String categoryname) {
        return categoryname != null ? product.categoryname.contains(categoryname) : null;
    }

}
