package com.user.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.user.model.Search;
import com.user.model.User;
import com.user.service.EmailService;
import com.user.service.HttpService;
import com.user.service.UserService;

@Controller
public class RegisterController {
	
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserService userService;
	private EmailService emailService;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder,
			UserService userService, EmailService emailService) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userService = userService;
		this.emailService = emailService;
	}
	
	// Return registration form template
	@RequestMapping(value="/register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, User user){
		modelAndView.addObject("user", user);
		
		//Giphy API
		Map<String, Object> resultMapObj =  httpService.getSearchData(null);
		List<Map<String,Object>> imageListObj = new ArrayList<Map<String,Object>>(); 
		
		try {
			if (resultMapObj != null && resultMapObj.get("data") != null) {
				ObjectMapper resultOutputMap = new ObjectMapper();
				String jsonResultOutput = resultOutputMap.writeValueAsString(resultMapObj.get("data"));
				List<Map<String,Object>> resultObj = resultOutputMap.readValue(jsonResultOutput, List.class);
				
				for(Map<String, Object> dataObj : resultObj) {
					ObjectMapper imageMap = new ObjectMapper();
					String jsonImageOutput = imageMap.writeValueAsString(dataObj.get("images"));
					Map<String,Object> imageMapObj = resultOutputMap.readValue(jsonImageOutput, Map.class);
					String jsonImageFixedOutput = imageMap.writeValueAsString(imageMapObj.get("fixed_height_still"));
					Map<String,Object> imageFixedObj = resultOutputMap.readValue(jsonImageFixedOutput, Map.class);
					Map<String, Object> imageGy = new HashMap<String, Object>();
					imageGy.put("url", imageFixedObj.get("url"));
					imageGy.put("width", imageFixedObj.get("width"));
					imageGy.put("height", imageFixedObj.get("height"));
					imageListObj.add(imageGy);  							
				}
			
			}
			System.out.println("imageListObj::"+imageListObj);
			modelAndView.addObject("imageListObj", imageListObj);     
		} catch (Exception ex) {
			
		}

		modelAndView.setViewName("register");
		return modelAndView;
	}
	
	// Process form input data
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid User user, BindingResult bindingResult, HttpServletRequest request) {
		
		
		
		// Lookup user in database by e-mail
		User userExists = userService.findByEmail(user.getEmail());
		
		System.out.println(userExists);
		
		if (userExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("register");		
		} else { // new user so we create user and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
		    user.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    user.setConfirmationToken(UUID.randomUUID().toString());
		    System.out.println("user::"+user);    
		    userService.saveUser(user);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(user.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + "/confirm?token=" + user.getConfirmationToken());
			registrationEmail.setFrom("noreply@domain.com");      
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + user.getEmail());
			modelAndView.setViewName("register");
		}
			
		return modelAndView;
	}
	
	// Process confirmation link
	@RequestMapping(value="/confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		User user = userService.findByConfirmationToken(token);
			
		if (user == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", user.getConfirmationToken());
		}
			
		modelAndView.setViewName("confirm");
		return modelAndView;		
	}
	
	
	// Process confirmation link
	@RequestMapping(value="/confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token")); 
			return modelAndView;
		}
	
		// Find the user associated with the reset token
		User user = userService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		user.setEnabled(true);
		
		// Save user
		userService.saveUser(user);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		return modelAndView;		
	}
	
	// List Users
	@RequestMapping(value="/users", method = RequestMethod.GET)
	public ModelAndView listUsers(ModelAndView modelAndView){
		List<User> users = userService.findAll();
		modelAndView.addObject("users", users);
		modelAndView.setViewName("list");
		return modelAndView;
	}
	
	// Return registration form template
	@RequestMapping(value="/edituser", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, @RequestParam("id") Integer id){
		System.out.println("EDIT USER....");
		System.out.println("ID::"+id);
		User user = userService.findById(id);
		//Giphy API
		Map<String, Object> resultMapObj =  httpService.getSearchData(null);
		List<Map<String,Object>> imageListObj = new ArrayList<Map<String,Object>>(); 
		
		try {
			if (resultMapObj != null && resultMapObj.get("data") != null) {
				ObjectMapper resultOutputMap = new ObjectMapper();
				String jsonResultOutput = resultOutputMap.writeValueAsString(resultMapObj.get("data"));
				List<Map<String,Object>> resultObj = resultOutputMap.readValue(jsonResultOutput, List.class);
				
				for(Map<String, Object> dataObj : resultObj) {
					ObjectMapper imageMap = new ObjectMapper();
					String jsonImageOutput = imageMap.writeValueAsString(dataObj.get("images"));
					Map<String,Object> imageMapObj = resultOutputMap.readValue(jsonImageOutput, Map.class);
					String jsonImageFixedOutput = imageMap.writeValueAsString(imageMapObj.get("fixed_height_still"));
					Map<String,Object> imageFixedObj = resultOutputMap.readValue(jsonImageFixedOutput, Map.class);
					Map<String, Object> imageGy = new HashMap<String, Object>();
					imageGy.put("url", imageFixedObj.get("url"));
					imageGy.put("width", imageFixedObj.get("width"));
					imageGy.put("height", imageFixedObj.get("height"));
					imageListObj.add(imageGy);  							
				}
			
			}
			System.out.println("imageListObj::"+imageListObj);
			modelAndView.addObject("imageListObj", imageListObj);     
		} catch (Exception ex) {
			
		}
			 	       
				
				
		
		
		modelAndView.setViewName("edit_user");
		return modelAndView;
	}
	
	
	
	// Return registration form template
	@RequestMapping(value="/search", method = RequestMethod.GET)
	public ModelAndView showSearchPage(ModelAndView modelAndView, Search search){
		modelAndView.addObject("search", search);
		
		//Giphy API
		Map<String, Object> resultMapObj =  httpService.getSearchData(search.getSearchString());
		List<Map<String,Object>> imageListObj = new ArrayList<Map<String,Object>>(); 
		
		try {
			if (resultMapObj != null && resultMapObj.get("data") != null) {
				ObjectMapper resultOutputMap = new ObjectMapper();
				String jsonResultOutput = resultOutputMap.writeValueAsString(resultMapObj.get("data"));
				List<Map<String,Object>> resultObj = resultOutputMap.readValue(jsonResultOutput, List.class);
				
				for(Map<String, Object> dataObj : resultObj) {
					ObjectMapper imageMap = new ObjectMapper();
					String jsonImageOutput = imageMap.writeValueAsString(dataObj.get("images"));
					Map<String,Object> imageMapObj = resultOutputMap.readValue(jsonImageOutput, Map.class);
					String jsonImageFixedOutput = imageMap.writeValueAsString(imageMapObj.get("fixed_height_still"));
					Map<String,Object> imageFixedObj = resultOutputMap.readValue(jsonImageFixedOutput, Map.class);
					Map<String, Object> imageGy = new HashMap<String, Object>();
					imageGy.put("url", imageFixedObj.get("url"));
					imageGy.put("width", imageFixedObj.get("width"));
					imageGy.put("height", imageFixedObj.get("height"));
					imageListObj.add(imageGy);  							
				}
			
			}
			System.out.println("imageListObj::"+imageListObj);
			modelAndView.addObject("imageListObj", imageListObj);     
		} catch (Exception ex) {
			
		}
		modelAndView.setViewName("search");
		return modelAndView;
	}
	
	
	// Process form input data
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public ModelAndView processSearchForm(ModelAndView modelAndView, @Valid Search search, BindingResult bindingResult, HttpServletRequest request) {
		System.out.println("search.getSearchString()::"+search.getSearchString());      
	    
		//Giphy API
		Map<String, Object> resultMapObj =  httpService.getSearchData(search.getSearchString());
		List<Map<String,Object>> imageListObj = new ArrayList<Map<String,Object>>(); 
		
		try {
			if (resultMapObj != null && resultMapObj.get("data") != null) {
				ObjectMapper resultOutputMap = new ObjectMapper();
				String jsonResultOutput = resultOutputMap.writeValueAsString(resultMapObj.get("data"));
				List<Map<String,Object>> resultObj = resultOutputMap.readValue(jsonResultOutput, List.class);
				
				for(Map<String, Object> dataObj : resultObj) {
					ObjectMapper imageMap = new ObjectMapper();
					String jsonImageOutput = imageMap.writeValueAsString(dataObj.get("images"));
					Map<String,Object> imageMapObj = resultOutputMap.readValue(jsonImageOutput, Map.class);
					String jsonImageFixedOutput = imageMap.writeValueAsString(imageMapObj.get("fixed_height_still"));
					Map<String,Object> imageFixedObj = resultOutputMap.readValue(jsonImageFixedOutput, Map.class);
					Map<String, Object> imageGy = new HashMap<String, Object>();
					imageGy.put("url", imageFixedObj.get("url"));
					imageGy.put("width", imageFixedObj.get("width"));
					imageGy.put("height", imageFixedObj.get("height"));
					imageListObj.add(imageGy);  							
				}
			
			}
			System.out.println("imageListObj::"+imageListObj);
			modelAndView.addObject("imageListObj", imageListObj);     
		} catch (Exception ex) {
			
		}
		
		modelAndView.setViewName("search");
			
		return modelAndView;
	}
	
	
}