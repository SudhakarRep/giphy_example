package com.user.controller;

import java.security.Principal;
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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.user.model.Category;
import com.user.model.Search;
import com.user.model.User;
import com.user.model.UserGiphy;
import com.user.service.CategoryService;
import com.user.service.EmailService;
import com.user.service.HttpService;
import com.user.service.UserGiphyService;
import com.user.service.UserService;

@Controller
public class RegisterController {
	
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserService userService;
	private EmailService emailService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private UserGiphyService userGiphySerivce;
	
	@Autowired
	public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder,
			UserService userService, EmailService emailService) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userService = userService;
		this.emailService = emailService;
		
	}
	
	  @RequestMapping(value="/loginError")
	  public String loginError(Model model, String username ){
	    model.addAttribute("error", "Your username and password is invalid.");
	    model.addAttribute("username",username);
	    return "login";
	  }
	  
	  
	  
	  @RequestMapping("/403")
	  public String access(){
	    return "/access";
	  }
	
	  @RequestMapping("/login")
	  public String login(Model model, String error, String logout, HttpServletRequest request ){
	    if (logout != null){
	      model.addAttribute("logout", "You have been logged out successfully.");
	    }
	    return "login";
	  }
	  
	  @RequestMapping(value="/main")
	  public String main(){
	    return "main";
	  }
	  
	  @RequestMapping("/admin")
	  public String admin(){
	    return "/admin/admin";
	  }
	  
	  @RequestMapping("/user")
	  public String user(){
	    return "/user/user";
	  }
	  	  
		// Return registration form template
		@GetMapping(value="/user/giphy/add")
		public ModelAndView showGiphyAdd(ModelAndView modelAndView, UserGiphy userGiphy,HttpServletRequest request){								
			return userService.userrGiphyAdd(modelAndView, userGiphy, request);
		}

		// Return registration form template
		@PostMapping(value="/user/giphy/add", params="searchImg")
		public ModelAndView showGiphyAddSearchImg(ModelAndView modelAndView, UserGiphy userGiphy){			
			return userService.userGiphyAddAPI(modelAndView, userGiphy);
		}

		// Return registration form template
		@GetMapping(value="/user/category/add")
		public ModelAndView addUserCategory(ModelAndView modelAndView, Category category){			
			return userService.userCategoryAddAPI(modelAndView, category);
		}

		// Return registration form template
		@PostMapping(value="/user/category/add")
		public ModelAndView saveUserCategory(ModelAndView modelAndView, Category category,HttpServletRequest request){	
			System.out.println("userCategory::"+category.toString());
			return userService.userCategorySave(modelAndView, category, request);
		}
		
		// Return registration form template
		@GetMapping(value="/user/categories")
		public ModelAndView userCategoryList(ModelAndView modelAndView, Category category,HttpServletRequest request){			
			return userService.userCategoryList(modelAndView, category, request);
		}
		
		// Return registration form template
		@PostMapping(value="/user/giphy/add",params="saveImage")
		public ModelAndView showGiphyAddPost(ModelAndView modelAndView, UserGiphy userGiphy, HttpServletRequest request){			
			return userService.userGiphySaveAndList(modelAndView, userGiphy, request);
		}

		// Return registration form template
		@GetMapping(value="/user/giphy/list")
		public ModelAndView showGiphyList(ModelAndView modelAndView, UserGiphy userGiphy, HttpServletRequest request){
			modelAndView.addObject("userGiphy", userGiphy);
			modelAndView.addObject("errorMessage", "");
			
			Principal principal = request.getUserPrincipal();
			User user = userService.findByEmail(principal.getName());
			userGiphy.setUserId(user.getId());
			
			List<UserGiphy> userGiphyListObj = userGiphySerivce.findByUserId(user.getId());
			List<Map<String, Object>> userGiphyList = new ArrayList<Map<String, Object>>();
			int inc = 0;
			for (UserGiphy userGiphyObj : userGiphyListObj) {
				inc=inc+1;
				Map<String, Object> userGiphyMap = new HashMap<String, Object>();
				userGiphyMap.put("searchString", userGiphyObj.getSearchString());
				userGiphyMap.put("giphyUrl", userGiphyObj.getGiphyUrl());
				userGiphyMap.put("id", userGiphyObj.getId());

				Category category = categoryService.findById(userGiphyObj.getCategoryId());
				userGiphyMap.put("categoryName", category.getName());
				userGiphyMap.put("slNo", inc);		
				userGiphyList.add(userGiphyMap);
			}
			modelAndView.addObject("userGiphyList", userGiphyList);
						
			modelAndView.setViewName("/user/user_giphy_list");
			return modelAndView;
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
	
   

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "/error/access-denied";
    }

	
	
}