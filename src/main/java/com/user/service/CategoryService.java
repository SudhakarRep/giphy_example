package com.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.user.model.Category;
import com.user.repository.CategoryRepository;

@Service("categoryService")
public class CategoryService {

	private CategoryRepository categoryRepository;

	@Autowired
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
		
	public List<Category> findAll() {
		return categoryRepository.findAll();
	}
	
	public Category findById(long id) {
		return categoryRepository.findOne(id);
	}
	
	
	public void saveCategory(Category category) {
		categoryRepository.save(category);
	}
	


}