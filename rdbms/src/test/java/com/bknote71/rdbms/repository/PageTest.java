package com.bknote71.rdbms.repository;

import com.bknote71.rdbms.model.Post;
import com.bknote71.rdbms.page.CursorPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StopWatch;

import java.time.LocalDate;

@SpringBootTest
class PageTest {

    @Autowired PagePostRepository pagePostRepository;

    @Test
    void legacy() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 이거 하나에 1200 ms
        // --> offset 이 커지면 커질수록 속도가 굉장히 느려진다.
        pagePostRepository.findAll(1000000, 2);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    void NoOffset() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CursorPage<Post> postCursorPage = null;
//        for (int i = 0; i < 1000; ++i) {
//            if (postCursorPage == null) {
//                postCursorPage = postPageRepository.paginationNoOffset(null, 2);
//            } else {
//                postPageRepository.paginationNoOffset(postCursorPage.getNextId(), 2);
//            }
//        }

        // 이거 하나에 20 ms 굉장히 빠름
        // 데이터가 1억건 이상일 시 기존 offset, limit 방식보다 수백배의 성능차이가 발생할 수 있다.
        // 데이터가 더 많아지면 더 성능차이가 심해진다!
        pagePostRepository.findAllNoOffset(999999L, 2);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    void coveringIndex() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 270 ms <<
        // 데이터 양이 많아지고, 페이지 번호가 뒤로 갈수록 NoOffset에 비해 느리다.
        // 테이블 사이즈가 계속 커지면 NoOffset 방식에 비해서는 성능 차이가 발생
        pagePostRepository.findAllCoveringIndex(1000000, 2);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    void realCoveringIndex() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 600 ms
        LocalDate f = LocalDate.of(2023, 01, 25);
        LocalDate s = LocalDate.of(2023, 01, 28);
        pagePostRepository.findBetweenDate(f, s, PageRequest.of(1000000, 2));

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    void pageWithFixCount() {

        LocalDate f = LocalDate.of(2023, 01, 25);
        LocalDate s = LocalDate.of(2023, 01, 28);
        Page<Post> page = pagePostRepository.findBetweenDateFixCount(false, f, s, PageRequest.of(1000001, 2));
        System.out.println(page.getNumber());
    }

}