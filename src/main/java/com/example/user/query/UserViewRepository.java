package com.example.user.query;

import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
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

	@Transactional
	public void save(UserView userView) {
		this.jdbcClient.sql("""
				INSERT INTO user_view (user_id, email, password) VALUES (:userId, :email, :password)
				""".trim()).paramSource(new BeanPropertySqlParameterSource(userView)).update();
	}

}
