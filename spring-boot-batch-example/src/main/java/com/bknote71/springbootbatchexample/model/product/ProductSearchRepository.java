package com.bknote71.BootBatch.model.product;

import java.util.List;

public interface ProductSearchRepository {
    List<Product> findBySearchCondition(ProductSearchCondition cond);
}
