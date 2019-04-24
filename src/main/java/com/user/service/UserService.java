package com.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.user.model.User;
import com.user.repository.UserRepository;

@Service("userService")
public class UserService {

	private UserRepository userRepository;

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

}