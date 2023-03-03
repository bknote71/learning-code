package com.bknote71.springbootbatch;

import com.bknote71.springbootbatch.domain.post.Post;
import com.bknote71.springbootbatch.domain.post.PostRepository;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DateInitializer {

    @Autowired PostRepository postRepository;

    @Test
    public void data_init() {
        LocalDate min = LocalDate.of(2023, 2, 2);
        LocalDate max = LocalDate.of(2023, 3, 3);

        EasyRandom easyRandom = createdRandomPost(min, max);
        int size = 100000;
        List<Post> posts = IntStream.range(0, size)
                .parallel()
                .mapToObj(i -> easyRandom.nextObject(Post.class))
                .toList();

        postRepository.bulkInsert(posts);
    }

    EasyRandom createdRandomPost(LocalDate min, LocalDate max) {
        Predicate<Field> idPredicate = FieldPredicates.named("id")
                .and(FieldPredicates.ofType(Long.class))
                .and(FieldPredicates.inClass(Post.class));

        EasyRandomParameters params = new EasyRandomParameters()
                .excludeField(idPredicate)
                .dateRange(min, max);

        return new EasyRandom(params);
    }

}
