package com.devsuperior.dsmovie.services;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository repository;

	private Long existingMovieId;
	private Long dependentMovieId;
	private Long nonExistingMovieId;
	private String validMovieTitle;
	private MovieEntity movie;
	private MovieDTO movieDTO;
	private PageImpl<MovieEntity> page;

	@BeforeEach
	void setUp() throws Exception {

		existingMovieId = 1L;
		dependentMovieId = 2L;
		nonExistingMovieId = 999L;

		movie = MovieFactory.createMovieEntity();
		validMovieTitle = movie.getTitle();
		movieDTO = new MovieDTO(movie);
		page = new PageImpl<>((List.of(movie)));

		Mockito.when(repository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		Mockito.when(repository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

		Mockito.when(repository.searchByTitle(any(), (Pageable) any())).thenReturn(page);

		Mockito.when(repository.findAll((Pageable) any())).thenReturn(page);

		Mockito.when(repository.save(any())).thenReturn(movie);

		Mockito.when(repository.getReferenceById(existingMovieId)).thenReturn(movie);
		Mockito.when(repository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.existsById(existingMovieId)).thenReturn(true);
		Mockito.when(repository.existsById(dependentMovieId)).thenReturn(true);
		Mockito.when(repository.existsById(nonExistingMovieId)).thenReturn(false);

		Mockito.doNothing().when(repository).deleteById(existingMovieId);
		Mockito.doThrow(DataIntegrityViolationException.class)
				.when(repository).deleteById(dependentMovieId);

	}

	@Test
	public void findAll_ShouldReturnPagedMovieDTO_WhenAllOk() {

		Pageable pageable = PageRequest.of(0, 12);

		Page<MovieDTO> result = service.findAll(validMovieTitle, pageable);

		Assertions.assertNotNull(result);
		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals(1, result.getTotalElements());
		Assertions.assertEquals(validMovieTitle, result.getContent().get(0).getTitle());

	}

	@Test
	public void findById_ShouldReturnMovieDTO_WhenIdExists() {

		MovieDTO result = service.findById(existingMovieId);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), validMovieTitle);

	}

	@Test
	public void findById_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingMovieId);
		});

	}

	@Test
	public void insert_ShouldReturnMovieDTO_WhenValidData() {

		MovieDTO result = service.insert(movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), validMovieTitle);

	}

	@Test
	public void update_ShouldReturnMovieDTO_WhenIdExists() {

		String updateTitle = "Runing Over Fire";
		MovieDTO updateMovieDTO = new MovieDTO(existingMovieId, updateTitle, 5.0, 0,
				"https://example.com/updated-image.jpg");

		MovieDTO result = service.update(existingMovieId, updateMovieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), updateTitle);

	}

	@Test
	public void update_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingMovieId, movieDTO);
		});

	}

	@Test
	public void delete_ShouldDoNothing_WhenIdExists() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingMovieId);
		});

	}

	@Test
	public void delete_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingMovieId);
		});

	}

	@Test
	public void delete_ShouldThrowDatabaseException_WhenDependentId() {

		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentMovieId);
		});

	}
}
