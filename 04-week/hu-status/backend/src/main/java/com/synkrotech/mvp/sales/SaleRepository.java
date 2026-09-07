package com.synkrotech.mvp.sales;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    /**
     * Fetches customer, lines and products up front: the response DTO touches
     * all of them, and doing it lazily would fire a query per line.
     */
    @Query("""
            select s from Sale s
              join fetch s.customer
              left join fetch s.details d
              left join fetch d.product
            order by s.date desc
            """)
    List<Sale> findAllForHistory();

    @Query("""
            select s from Sale s
              join fetch s.customer
              left join fetch s.details d
              left join fetch d.product
            where s.id = :id
            """)
    Optional<Sale> findByIdWithDetails(UUID id);
}
