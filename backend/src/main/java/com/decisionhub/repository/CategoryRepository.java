package com.decisionhub.repository;

import com.decisionhub.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CategoryRepository extends JpaRepository<Category, Long> {
}