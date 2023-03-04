package com.bknote71.springbootbatch.config.chunk.reader;

import com.bknote71.springbootbatch.domain.post.Post;
import com.bknote71.springbootbatch.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ItemReaderAdapterJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PostRepository postRepository;

    private int chunkSize = 10;
    @Bean
    public Job itemReadAdapterJob() {
        return jobBuilderFactory.get("itemReadAdapterJob")
                .incrementer(new RunIdIncrementer())
                .start(itemReadAdapterStep())
                .build();
    }

    @Bean
    public Step itemReadAdapterStep() {
        return stepBuilderFactory.get("itemReadAdapterStep")
                .<Post, Post>chunk(chunkSize)
                .reader(itemReader())
                .writer(System.out::println)
                .build();
    }

    // 왜케 느리지??
    private ItemReader<? extends Post> itemReader() {
        ItemReaderAdapter<Post> reader = new ItemReaderAdapter<>();
        reader.setTargetObject(postRepository);
        reader.setTargetMethod("findAll");
        return reader;
    }
}
