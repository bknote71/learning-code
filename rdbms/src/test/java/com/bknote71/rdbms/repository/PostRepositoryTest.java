package com.bknote71.rdbms.repository;

import com.bknote71.rdbms.model.DailyPostCount;
import com.bknote71.rdbms.model.Post;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@SpringBootTest
class PostRepositoryTest {

    @Autowired PostRepository postRepository;

    @Test
    void init() {
        // easy random 사용
        EasyRandom randomPost = createRandomPost(
                2L,
                LocalDate.of(2023, 1, 25),
                LocalDate.of(2023, 1, 30));
        int size = 1000000;
        List<Post> posts = IntStream.range(0, size * 2)
                .parallel()
                .mapToObj(i -> randomPost.nextObject(Post.class))
                .toList();
        // 2백만건의 bulk insert: 7분 14초
        postRepository.bulkInsert(posts);
    }

    @Test
    void indexOffTest() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 0.48초 정도 (480ms)
        String content = "A";
        List<Post> posts = postRepository.findByContentsStartWith(content);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
    }

    @Test
    void indexOnTest() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 200만 건 기준
        // 없는 memberId(1L) 일 경우 0.02 초 <--> 인덱스 미사용 시 0.7초
        // 있는 memberId(2L) 일 경우 8초 <--> 인덱스 미사용 시 3초 << 오히려 인덱스 사용 때문에 더 오래걸린다.
        // 인덱스를 사용했음에도 더 오래 걸릴 수 있는 이유?
        // - 인덱스를 통해 필터링하는 데이터가 매우 적은 경우 인덱스를 검사하는데 발생하는 오버헤드 때문에 더 오래걸린다.
        // + 여러 인덱스 페이지를 탐색하기 때문 (페이지는 16KB 로 크기가 고정)
        Long nonMemberId = 1L;
        Long memberId = 2L;
        List<Post> posts = postRepository.findByMemberId(memberId);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
    }

    @Test
    void searchOnIndex() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Long memberId = 2L;
        LocalDate sDate = LocalDate.of(2023, 1, 27);
        LocalDate eDate = LocalDate.of(2023, 1, 30);

        List<DailyPostCount> posts = postRepository.groupByCreatedDate(memberId, sDate, eDate);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
    }





    EasyRandom createRandomPost(Long memberId, LocalDate min, LocalDate max) {

        Predicate<Field> idPredicate = FieldPredicates.named("id")
                .and(FieldPredicates.ofType(Long.class))
                .and(FieldPredicates.inClass(Post.class));


        Predicate<Field> mIdPredicate = FieldPredicates.named("memberId")
                .and(FieldPredicates.ofType(Long.class))
                .and(FieldPredicates.inClass(Post.class));

        EasyRandomParameters params = new EasyRandomParameters()
                .excludeField(idPredicate)
                .dateRange(min, max)
                .randomize(mIdPredicate, () -> memberId);

        return new EasyRandom(params);
    }
}