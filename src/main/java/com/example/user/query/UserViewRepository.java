package com.example.user.query;

import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class UserViewRepository {

	private final JdbcClient jdbcClient;

	public UserViewRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public Optional<UserView> findByUserId(UUID userId) {
		return this.jdbcClient.sql("""
				SELECT user_id, email, password FROM user_view WHERE user_id = :userId
				""".trim()).param("userId", userId).query(UserView.class).optional();
	}

	public Optional<UserView> findByEmail(String email) {
		return this.jdbcClient.sql("""
				SELECT user_id, email, password FROM user_view WHERE email = :email
				""".trim()).param("email", email).query(UserView.class).optional();
	}

}
