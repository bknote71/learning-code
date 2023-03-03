package com.bknote71.springbootbatch.domain.post;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void bulkInsert(List<Post> posts) {
        String sql = """
                insert into POST(title, content, created_at)
                values (:title, :content, :created_at)
                """;

        SqlParameterSource[] batchArgs = posts.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
