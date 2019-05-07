package com.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.user.model.UserGiphy;
import com.user.repository.UserGiphyRepository;

@Service("userGiphyService")
public class UserGiphyService {

	private UserGiphyRepository userGiphyRepository;

	@Autowired
	public UserGiphyService(UserGiphyRepository userGiphyRepository) {
		this.userGiphyRepository = userGiphyRepository;
	}
	
	public List<UserGiphy> findByUserId(Integer userId) {
		return userGiphyRepository.findByUserId(userId);
	}
	
	public UserGiphy findByUserIdAndGiphyUrl(Integer userId, String giphyUrl) {
		return userGiphyRepository.findByUserIdAndGiphyUrl(userId, giphyUrl);
	}
		
	public void saveUserGiphy(UserGiphy userGiphy) {
		userGiphyRepository.save(userGiphy);
	}
	


}