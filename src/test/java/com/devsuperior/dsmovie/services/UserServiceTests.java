package com.devsuperior.dsmovie.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CustomUserUtil userUtil;

	private String existingUsername;
	private String nonExistingUsername;

	private UserEntity user;
	private List<UserDetailsProjection> userdetailsProjectionList;

	@BeforeEach
	public void setUp() throws Exception {

		existingUsername = "maria@gmail.com";
		nonExistingUsername = "user@gmail.com";

		user = UserFactory.createUserEntity();
		userdetailsProjectionList = UserDetailsFactory.createCustomAdminUser(existingUsername);

		Mockito.when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(user));
		Mockito.when(userRepository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

		Mockito.when(userRepository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userdetailsProjectionList);
		Mockito.when(userRepository.searchUserAndRolesByUsername(nonExistingUsername)).thenReturn(List.of());

	}

	@Test
	public void authenticated_ShouldReturnUserEntity_WhenUserExists() {

		Mockito.when(userUtil.getLoggedUsername()).thenReturn(existingUsername);
		UserEntity result = service.authenticated();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), user.getId());
		Assertions.assertEquals(result.getUsername(), existingUsername);

	}

	@Test
	public void authenticated_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExists() {

		Mockito.doThrow(ClassCastException.class).when(userUtil).getLoggedUsername();
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.authenticated();
		});

	}

	@Test
	public void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {

		UserDetails result = service.loadUserByUsername(existingUsername);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getUsername(), existingUsername);

	}

	@Test
	public void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExists() {

		UserService spyService = Mockito.spy(service);
		Mockito.doReturn(user).when(spyService).authenticated();

		Mockito.doThrow(ClassCastException.class).when(userUtil).getLoggedUsername();
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			spyService.loadUserByUsername(nonExistingUsername);
		});

	}
}
