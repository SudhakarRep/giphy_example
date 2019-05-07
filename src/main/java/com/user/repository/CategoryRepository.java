package com.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.user.model.Category;

@Repository("categoryRepository")
public interface CategoryRepository extends CrudRepository<Category, Long> {
	 List<Category> findAll();
	 Category findById(Long id); 
}