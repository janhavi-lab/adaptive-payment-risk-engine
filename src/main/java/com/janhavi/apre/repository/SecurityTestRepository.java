package com.janhavi.apre.repository;

import com.janhavi.apre.entity.SecurityTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityTestRepository extends JpaRepository<SecurityTest, Long> {

    Optional<SecurityTest> findByTestId(String testId);

    List<SecurityTest> findTop10ByOrderByCreatedAtDesc();

    Page<SecurityTest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
