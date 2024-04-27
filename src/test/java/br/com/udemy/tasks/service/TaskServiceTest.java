package br.com.udemy.tasks.service;

import br.com.udemy.tasks.model.Task;
import br.com.udemy.tasks.repository.TaskRepository;
import br.com.udemy.tasks.repository.custom.TaskCustomRepository;
import br.com.udemy.tasks.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCustomRepository taskCustomRepository;

    @Test
    public void service_mustReturnTask_whenInsertSucessfully(){
        Task task = TestUtils.buildValidTask();

        when(taskRepository.save(any())).thenReturn(Mono.just(task));

        StepVerifier.create(taskService.insert(task))
                .then(() -> verify(taskRepository, times(1)).save(any()))
                .expectNext(task)
                .expectComplete();

    }


    @Test
    public void service_mustReturnVoid_whenDeleteTaskSucessfully(){

        when(taskRepository.deleteById(anyString())).thenReturn(Mono.empty());
        StepVerifier.create(taskService.deleteById("someId"))
                .then(() -> verify(taskRepository, times(1)).deleteById(anyString()))
                .verifyComplete();
    }

    @Test
    public void service_mustReturnTaskPage_whenFindPaginated(){
        Task task = TestUtils.buildValidTask();

        when(taskCustomRepository.findPaginated(any(), anyInt(), anyInt())).thenReturn(Mono.just(Page.empty()));
        Mono<Page<Task>> result = taskService.findPaginated(task, 0, 10);

        assertNotNull(result);

        verify(taskCustomRepository, times(1)).findPaginated(any(), anyInt(), anyInt());

    }


}
