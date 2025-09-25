package com.devsuperior.dsmovie.services;

import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

	@InjectMocks
	private ScoreService scoreService;

	@Mock
	private UserService userService;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private ScoreRepository scoreRepository;

	private Long existingMovieId;
	private Long nonExistingMovieId;
	private UserEntity user;
	private MovieEntity movie;
	private ScoreEntity score;

	@BeforeEach
	void setUp() throws Exception {

		existingMovieId = 1L;
		nonExistingMovieId = 1000L;

		user = UserFactory.createUserEntity();
		movie = MovieFactory.createMovieEntity();
		score = ScoreFactory.createScoreEntity();
		// for test sum scores
		movie.getScores().add(score);

		Mockito.when(userService.authenticated()).thenReturn(user);
		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());
		Mockito.when(scoreRepository.saveAndFlush(any())).thenReturn(score);
		Mockito.when(movieRepository.save(any())).thenReturn(movie);

	}

	@Test
	public void saveScore_ShouldReturnMovieDTO_WhenValidData() {

		MovieDTO result = scoreService.saveScore(new ScoreDTO(score));

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getTitle(), movie.getTitle());
		Assertions.assertEquals(result.getScore(), movie.getScore());

	}

	@Test
	public void saveScore_ShouldThrowResourceNotFoundException_WhenNonExistingMovieId() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			scoreService.saveScore(new ScoreDTO(nonExistingMovieId, 5.0));
		});
	}

}
