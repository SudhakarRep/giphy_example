package com.user.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.user.model.Category;
import com.user.model.User;
import com.user.model.UserGiphy;
import com.user.service.CategoryService;
import com.user.service.UserGiphyService;
import com.user.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserGiphyService userGiphyService;
		
	@Test
	@WithMockUser(roles = "USER")
	public void loginPageIsLoadedOk() throws Exception
	{
	    mockMvc.perform(MockMvcRequestBuilders.get("/login")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString("username")));
	}
	
	
	@Test
	public void loginUrlIsWorking() throws Exception {
		//String basicDigestHeaderValue = "Basic " + new String(Base64.encodeBase64(("<username>:<password>").getBytes()));
		this.mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/login").header("Authorization", "").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());	
	}

	@Test
	public void loginUrlIsNotWorking() throws Exception {
		String basicDigestHeaderValue = "Basic " + new String(Base64.encodeBase64(("<username>:<password>").getBytes()));
		this.mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/login2").header("Authorization", basicDigestHeaderValue).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());	
	}

	@Test
	public void isCategoriesExist() {
		List<Category> categories = categoryService.findAll();
		Assert.assertNotNull(categories);
	}
	
	@Test
	public void isUserExist() {
		User user = userService.findByEmail("aman@gmail.com");
		Assert.assertNotNull(user);
	}
	
	@Test
	public void isUserHasCategories() {
		User user = userService.findByEmail("aman@gmail.com");
		List<Category> category = categoryService.findByUserId(user.getId());
		Assert.assertNotNull(category);
	}
	
	
	@Test
	public void isUserCategoryExist() {
		User user = userService.findByEmail("aman@gmail.com");
		Category category = categoryService.findByUserIdAndName(user.getId(), "funny");
		Assert.assertNotNull(category);
	}
	
	@Test
	public void isUserHasGiphies() {
		User user = userService.findByEmail("aman@gmail.com");
		List<UserGiphy> userGiphy = userGiphyService.findByUserId(user.getId());
		Assert.assertNotNull(userGiphy);
	}
	
	
}
