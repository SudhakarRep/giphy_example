package com.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.user.model.UserGiphy;

@Repository("userGiphyRepository")
public interface UserGiphyRepository extends CrudRepository<UserGiphy, Long> {
	List<UserGiphy> findByUserId(Integer userId);

	UserGiphy findByUserIdAndGiphyUrl(Integer userId, String giphyUrl);
}