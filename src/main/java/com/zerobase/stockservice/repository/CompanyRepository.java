package com.zerobase.stockservice.repository;

import com.zerobase.stockservice.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByTicker(String ticker);

    Optional<Company> findByName(String name);

    Optional<Company> findByTicker(String ticker);

    Page<Company> findByNameStartingWithIgnoreCase(String s, Pageable pageable);
}
