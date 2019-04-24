package com.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.user.model.User;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {
	 User findByEmail(String email);
	 User findByConfirmationToken(String confirmationToken);
	 List<User> findAll();
	 User findById(Integer id);
}