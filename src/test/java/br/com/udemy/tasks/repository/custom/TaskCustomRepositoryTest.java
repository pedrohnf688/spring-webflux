package br.com.udemy.tasks.repository.custom;

import br.com.udemy.tasks.model.Task;
import br.com.udemy.tasks.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;

@SpringBootTest
public class TaskCustomRepositoryTest {

    @InjectMocks
    private TaskCustomRepository taskCustomRepository;

    @Mock
    private ReactiveMongoOperations operations;


    @Test
    public void customRepository_mustReturnPageWithOneElement_whenSendTask(){
        Task task = TestUtils.buildValidTask();

        when(operations.find(any(), any())).thenReturn(Flux.just(task));
        when(operations.count(any(Query.class), eq(Task.class))).thenReturn(Mono.just(1L));
        Mono<Page<Task>> result = taskCustomRepository.findPaginated(task, 0, 10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, Objects.requireNonNull(result.block()).getNumberOfElements());

    }


}
