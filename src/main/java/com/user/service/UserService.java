package com.user.service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.user.model.Category;
import com.user.model.User;
import com.user.model.UserGiphy;
import com.user.model.UserGiphyPojo;
import com.user.repository.UserRepository;

@Service("userService")
public class UserService {

	private UserRepository userRepository;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private UserGiphyService userGiphySerivce;
	
	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public User findById(Integer id) {
		return userRepository.findById(id);
	}
	
	
	public User findByConfirmationToken(String confirmationToken) {
		return userRepository.findByConfirmationToken(confirmationToken);
	}
	
	public List<User> findAll() {
		return userRepository.findAll();
	}
	
	public void saveUser(User user) {
		userRepository.save(user);
	}
	
	public ModelAndView userGiphyAddAPI(ModelAndView modelAndView, UserGiphy userGiphy) {		
		modelAndView.addObject("userGiphy", userGiphy);		
		List<Category> categories = categoryService.findAll();
		modelAndView.addObject("categories", categories);
		modelAndView.addObject("errorMessage", "");		
		//Giphy API
		String searchString = (!StringUtils.isEmpty(userGiphy.getSearchString())) ? userGiphy.getSearchString() : ""; 
		List<Map<String,Object>> imageListObj = httpService.getImageProperties(searchString);
		modelAndView.addObject("imageListObj", imageListObj);  
		modelAndView.setViewName("/user/user_giphy_add");
		return modelAndView;
	}
	
	public ModelAndView userGiphySaveAndList(ModelAndView modelAndView, UserGiphy userGiphy, HttpServletRequest request) {
		modelAndView.addObject("userGiphy", userGiphy);
		
		Principal principal = request.getUserPrincipal();
		User user = this.findByEmail(principal.getName());
		userGiphy.setUserId(user.getId());
		
		UserGiphy userGiphyExisting = userGiphySerivce.findByUserIdAndGiphyUrl(user.getId(), userGiphy.getGiphyUrl());
		
		if (userGiphyExisting == null) {
			userGiphySerivce.saveUserGiphy(userGiphy);
			modelAndView.addObject("errorMessage", "Saved successfully");
		} else {
			modelAndView.addObject("errorMessage", "Image already selected");
		}
		List<UserGiphy> userGiphyListObj = userGiphySerivce.findByUserId(user.getId());
		List<UserGiphyPojo> userGiphyList = new ArrayList<UserGiphyPojo>();
		int inc = 0;
		for (UserGiphy userGiphyObj : userGiphyListObj) {
			inc=inc+1;
			UserGiphyPojo userGiphyPojo = new UserGiphyPojo();
			userGiphyPojo.setSearchString(userGiphyObj.getSearchString());
			userGiphyPojo.setGiphyUrl(userGiphyObj.getGiphyUrl());
			userGiphyPojo.setId(userGiphyObj.getId());

			Category category = categoryService.findById(userGiphyObj.getCategoryId());
			userGiphyPojo.setCategoryName(category.getName());
			userGiphyPojo.setSlNo(inc);		
			userGiphyList.add(userGiphyPojo);
		}
		modelAndView.addObject("userGiphyList", userGiphyList);					
		modelAndView.setViewName("/user/user_giphy_list");
		return modelAndView;
	}

	public ModelAndView userCategoryAddAPI(ModelAndView modelAndView, Category category) {
		modelAndView.addObject("category", category);
		modelAndView.setViewName("/user/user_category_add");
		return modelAndView;
	}

	public ModelAndView userCategorySave(ModelAndView modelAndView, Category category, HttpServletRequest request) {
		
		Principal principal = request.getUserPrincipal();
		User user = this.findByEmail(principal.getName());
		category.setUserId(user.getId());
		System.out.println("userCategory::"+category);
		Category categoryExisting = categoryService.findByUserIdAndName(user.getId(), category.getName());
		
		if (categoryExisting == null) {
			categoryService.saveCategory(category);
			modelAndView.addObject("errorMessage", "Saved successfully");
			
			List<Category> categoryListObj = categoryService.findByUserId(user.getId());
			modelAndView.addObject("userCategoryList", categoryListObj);	
			
			modelAndView.setViewName("/user/user_category_list");
		} else {
			modelAndView.addObject("errorMessage", "Category already exist");
			modelAndView.setViewName("/user/user_category_add");
		}
		
		return modelAndView;
	}

	public ModelAndView userCategoryList(ModelAndView modelAndView, Category category, HttpServletRequest request) {
		
		modelAndView.addObject("category", category);
		
		Principal principal = request.getUserPrincipal();
		User user = this.findByEmail(principal.getName());
		category.setUserId(user.getId());
		
		List<Category> userCategoryListObj = categoryService.findByUserId(user.getId());
		modelAndView.addObject("userCategoryList", userCategoryListObj);	
		
		modelAndView.setViewName("/user/user_category_list");
		return modelAndView;
	}

	public ModelAndView userrGiphyAdd(ModelAndView modelAndView, UserGiphy userGiphy, HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		User user = this.findByEmail(principal.getName());
				
		modelAndView.addObject("userGiphy", userGiphy);			
		List<Category> categories = categoryService.findByUserId(user.getId());
		modelAndView.addObject("categories", categories);			
		List<Map<String,Object>> imageListObj = new ArrayList<Map<String,Object>>();
		modelAndView.addObject("imageListObj", imageListObj);  
		modelAndView.addObject("errorMessage", "");
		modelAndView.setViewName("/user/user_giphy_add");	
		
		return modelAndView;
	}


}