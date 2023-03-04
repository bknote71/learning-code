package com.bknote71.springbootbatch.domain.post;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Post> MAPPER = (rs, rn) -> {
        long id = rs.getLong("id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Post(id, title, content, createdAt.toLocalDateTime());
    };

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

    public List<Post> findAll() {
        String sql = """
                select * 
                from Post                
                """;
        return jdbcTemplate.query(sql, (SqlParameterSource) null, MAPPER);
    }
    public List<Post> findByTitle(String title) {
        String sql = """
                select title 
                from post
                where title = :title                
                """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("title", title);
        return jdbcTemplate.query(sql, param, MAPPER);
    }
}
