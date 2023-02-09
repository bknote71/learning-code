package com.bknote71.BootBatch.model.product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductSearchRepository {
}
