package com.example.user.query;

import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * used temporally
 */
@Component
public class UserInitializer implements ApplicationRunner {

	private final JdbcClient jdbcClient;

	public UserInitializer(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		UserView userView = UserViewBuilder.userView()
			.userId(UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46"))
			.email("demo@example.com")
			.password("{noop}password}")
			.build();
		try {
			this.jdbcClient.sql("""
					INSERT INTO user_view (user_id, email, password) VALUES (:userId, :email, :password)
					""".trim()).paramSource(new BeanPropertySqlParameterSource(userView)).update();
		}
		catch (DuplicateKeyException ignored) {
		}
	}

}
