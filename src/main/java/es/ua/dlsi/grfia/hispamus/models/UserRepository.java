package es.ua.dlsi.grfia.hispamus.models;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
	public Optional<User> findByUsernameIgnoreCase(String username);
}
