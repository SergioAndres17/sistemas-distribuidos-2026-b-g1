package com.synkrotech.mvp.products;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /*
     * Category is a lazy association but every response carries its name, so
     * each read fetches it up front rather than relying on an open session.
     */

    @Query("select p from Product p join fetch p.category order by p.name asc")
    List<Product> findAllWithCategory();

    @Query("select p from Product p join fetch p.category where p.active = true order by p.name asc")
    List<Product> findActiveWithCategory();

    @Query("select p from Product p join fetch p.category where p.id = :id")
    Optional<Product> findByIdWithCategory(UUID id);
}
