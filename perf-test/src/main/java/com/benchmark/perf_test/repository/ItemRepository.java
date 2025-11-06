package com.benchmark.perf_test.repository;

import com.benchmark.perf_test.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends JpaRepository<Item, Long> {

    @RestResource(path = "by-category", rel = "by-category")
    Page<Item> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :cid")
    Page<Item> findByCategoryIdWithJoinFetch(@Param("cid") Long categoryId, Pageable pageable);
}
