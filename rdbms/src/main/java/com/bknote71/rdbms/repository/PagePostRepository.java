package com.bknote71.rdbms.repository;

import com.bknote71.rdbms.model.Post;
import com.bknote71.rdbms.page.CursorPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class PagePostRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    RowMapper<Post> ROW_MAPPER = (rs, rn) -> {
        long id = rs.getLong("id");
        long memberId = rs.getLong("memberId");
        String contents = rs.getString("contents");
        LocalDate createdDate = rs.getObject("createdDate", LocalDate.class);
        LocalDateTime createdAt = rs.getObject("createdAt", LocalDateTime.class);
        return new Post(id, memberId, contents, createdDate, createdAt);
    };

    // 기존 - offset, limit 방식
    // offset 숫자만큼 탐색이 이루어진다. << 굉장히 비효율적인 방식
    public List<Post> findAll(int page, int size) {
        String sql = """
                select * 
                from POST
                limit :limit
                offset :offset
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", page * size);

        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    // 커서 방식: no offset
    public CursorPage<Post> findAllNoOffset(Long id, int size) {
        // id의 존재 여부에 따라서 동적 쿼리가 필요
        String first = """
                select *
                from POST
                """;
        String ltId = " where id < :id";
        String end = " order by id desc limit :limit";

        String sql;
        if (id != null) {
            sql = first.concat(ltId).concat(end);
        } else {
            sql = first.concat(end);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("limit", size);

        List<Post> results = jdbcTemplate.query(sql, params, ROW_MAPPER);

        return new CursorPage<>(results, results.size() != 0 ? results.get(results.size() - 1).getId() : null);
    }

    // 커버링 인덱스란? where, order by, group by 등의 조건절에서 사용되는 모든 컬럼이 하나의 index 에 포함되는 경우 (하나의 쿼리 = 하나의 인덱스 사용)
    // 실제로 커버링 인덱스를 태우는 부분은 select 절을 제외한 나머지만 우선으로 탐색한다.
    // 모든 조건문 컬럼(항목)이 하나의 인덱스의 컬럼으로만 이루어지게 하여 인덱스 내부에서 쿼리가 완성될 수 있도록 한다.
    // 이렇게 커버링 인덱스로 빠르게 걸러낸 row의 id를 통해 실제 select 절의 항목들을 빠르게 조회해오는 방법!!
    // 일반적으로 조회 쿼리에서 가장 큰 성능저하를 일으키는 부분이 인덱스가 아닌 테이블(데이터 블록)에 접근할 때이다. (random io)
    // 즉 커버링 인덱스는 인덱스를 활용하여 빠르게 데이터를 거르고 걸러진 적은 수의 row에 대해서만 데이터 블록에 접근.
    // 어떻게 활용?
    // 1. 커버링 인덱스를 활용해 최종적으로 조회 대상의 PK를 조회
    // - 모든 PK는 인덱스에 자동으로 포함되기 때문.
    // 단점: 단일 쿼리의 모든 조건 항목이 인덱스에 포함되어야 하기 때문에 인덱스의 오버헤드가 발생한다.
    // 왜? 하나의 항목이라도 인덱스에 포함이 안되어있으면?
    // 테이블(random io)에 접근해서 그 항목에 대하여 하나하나 탐색해야 하고 최종적으로 limit, offset 을 적용할 수 있기 때문에.
    public List<Post> findAllCoveringIndex(int page, int size) {
        String sql = """
                SELECT *
                FROM POST as p
                JOIN (SELECT id
                        FROM POST
                        ORDER BY id desc
                        LIMIT :limit
                        OFFSET :offset) as temp on temp.id = p.id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", page * size);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public Page<Post> findBetweenDate(LocalDate firstDate, LocalDate secondDate, Pageable pageable) {
        String sql = """
                SELECT *
                FROM POST as p
                JOIN (SELECT id
                        FROM POST
                        WHERE createdDate BETWEEN :firstDate and :secondDate
                        ORDER BY id desc
                        LIMIT :limit
                        OFFSET :offset) as temp on temp.id = p.id
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("firstDate", firstDate);
        params.put("secondDate", secondDate);
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        List<Post> results = jdbcTemplate.query(sql, params, ROW_MAPPER);
        // not count query
        return new PageImpl<>(results, pageable, 0);
    }

    // 카운트 쿼리 2가지 최적화
    // 1. 카운트 쿼리 캐시
    // 2. 다음 버튼 눌렀을 때 실제 쿼리
    public Page<Post> findBetweenDateCountCache(LocalDate firstDate, LocalDate secondDate, Pageable pageable, Integer countCache) {
        String sql = """
                SELECT *
                FROM POST as p
                JOIN (SELECT id
                        FROM POST
                        WHERE createdDate BETWEEN :firstDate and :secondDate
                        ORDER BY id desc
                        LIMIT :limit
                        OFFSET :offset) as temp on temp.id = p.id
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("firstDate", firstDate);
        params.put("secondDate", secondDate);
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<Post> results = jdbcTemplate.query(sql, params, ROW_MAPPER);

        String countQuery = """
                SELECT count(*) as count
                FROM POST
                WHERE createdDate BETWEEN :firstDate and :secondDate             
                """;
        Map<String, Object> countParams = new HashMap<>();
        params.put("firstDate", firstDate);
        params.put("secondDate", secondDate);

        Integer count = countCache != null ? countCache : jdbcTemplate.queryForObject(sql, countParams, Integer.class);
        return new PageImpl<>(results, pageable, count);
    }

    public Page<Post> findBetweenDateFixCount(boolean search, LocalDate firstDate, LocalDate secondDate, Pageable pageable) {
        int fixedPageNumber = 10;
        String sql = """
                SELECT *
                FROM POST as p
                JOIN (SELECT id
                        FROM POST
                        WHERE createdDate BETWEEN :firstDate and :secondDate
                        ORDER BY id desc
                        LIMIT :limit
                        OFFSET :offset) as temp on temp.id = p.id
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("firstDate", firstDate);
        params.put("secondDate", secondDate);
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<Post> results = jdbcTemplate.query(sql, params, ROW_MAPPER);

        if (search) {
            // 사용자에게 보여지는 것은 10페이지 이므로 인자 100개밖에 없다고 생각되어져도 괜찮다.
            int fixedPageCount = fixedPageNumber * pageable.getPageSize();
            return new PageImpl<>(results, pageable, fixedPageCount);
        }

        // 요청: 페이지 사이즈가 10인 10번째 페이지 요청
        // 실제: 98번밖에 없음 << 9페이지
        // --> countQuery 조회, pageable 변경
        String countQuery = """
                SELECT count(*) as count
                FROM POST
                WHERE createdDate BETWEEN :firstDate and :secondDate             
                """;
        Map<String, Object> countParams = new HashMap<>();
        countParams.put("firstDate", firstDate);
        countParams.put("secondDate", secondDate);

        Integer totalCount = jdbcTemplate.queryForObject(countQuery, countParams, Integer.class);

        Pageable pageRequest = fixPageRequest(pageable, totalCount);
        return new PageImpl<>(results, pageRequest, totalCount);
    }

    private Pageable fixPageRequest(Pageable pageable, Integer totalCount) {
        // totalCount >= offset(페이지의 처음 번호, 0부터) + 1그대로 리턴
        // 아니면? 실제 페이지 번호를 반환해야 한다.
        // 실제 페이지 번호 = 토탈_카운트/페이지_사이즈
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int offset = page * size;
        if (totalCount >= offset + 1) {
            return pageable;
        }
        return PageRequest.of((totalCount - 1)/ size, size);
    }
}
