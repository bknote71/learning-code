package com.bknote71.rdbms.repository;

import com.bknote71.rdbms.model.DailyPostCount;
import com.bknote71.rdbms.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    RowMapper<Post> ROW_MAPPER = (rs, rn) -> {
        long id = rs.getLong("id");
        long memberId = rs.getLong("memberId");
        String contents = rs.getString("contents");
        LocalDate createdDate = rs.getObject("createdDate", LocalDate.class);
        LocalDateTime createdAt = rs.getObject("createdAt", LocalDateTime.class);
        return new Post(id, memberId, contents, createdDate, createdAt);
    };

    public void bulkInsert(List<Post> posts) {
        String sql = """
            insert into POST (memberId, contents, createdDate, createdAt)
            values (:memberId, :contents, :createdDate, :createdAt)
            """;

        SqlParameterSource[] params = posts.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }

    public List<Post> findByContentsStartWith(String contents) {
        String sql = """
                select * 
                from POST 
                where contents like ':contents'
                """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("contents", contents + "%");

        return jdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findByMemberId(Long memberId) {
        String sql = """
                SELECT *
                FROM POST
                WHERE memberId = :memberId
                """;
        Map<String, Long> param = new HashMap<>();
        param.put("memberId", memberId);
        return jdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<DailyPostCount> groupByCreatedDate(Long memberId, LocalDate sDate, LocalDate eDate) {
        String sql = """
                 SELECT createdDate, memberId, count(*) as count
                 FROM POST
                 WHERE memberId = :memberId and createdDate between :sDate and :eDate
                 GROUP BY memberId, createdDate
                 """;

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("sDate", sDate);
        params.put("eDate", eDate);

        RowMapper<DailyPostCount> rowMapper = ((rs, rowNum) -> new DailyPostCount(
                rs.getLong("memberId"),
                rs.getObject("createdDate", LocalDate.class),
                rs.getLong("count")));

        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
