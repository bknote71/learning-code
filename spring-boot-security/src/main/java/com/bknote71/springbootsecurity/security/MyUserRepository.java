package com.bknote71.springbootsecurity.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MyUserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MyUser findByUsername(String username) {
        String sql = """
                select *
                from User
                where username = :username 
                """;
        MapSqlParameterSource param = new MapSqlParameterSource("username", username);
        return jdbcTemplate.query(sql, param, rs -> {
            return new MyUser(rs.getString("username"), rs.getString("password"));
        });
    }
}
