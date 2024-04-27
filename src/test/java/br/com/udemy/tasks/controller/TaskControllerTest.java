package br.com.udemy.tasks.controller;

import br.com.udemy.tasks.controller.converter.TaskDTOConverter;
import br.com.udemy.tasks.controller.converter.TaskInsertDTOConverter;
import br.com.udemy.tasks.controller.dto.TaskDTO;
import br.com.udemy.tasks.controller.dto.TaskInsertDTO;
import br.com.udemy.tasks.model.Task;
import br.com.udemy.tasks.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@SpringBootTest
public class TaskControllerTest {

    @InjectMocks
    private TaskController taskController;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskDTOConverter converter;

    @Mock
    private TaskInsertDTOConverter insertDTOConverter;

    @Test
    public void controller_mustReturnOk_whenSaveSucessfully(){
        when(taskService.insert(any())).thenReturn(Mono.just(new Task()));
        when(converter.convert(any(Task.class))).thenReturn(new TaskDTO());

        WebTestClient client = WebTestClient.bindToController(taskController).build();

        client.post()
                .uri("/task")
                .bodyValue(new TaskInsertDTO())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskDTO.class);
    }

    @Test
    public void controller_mustReturnOk_whenGetPaginagedSucessfully() {
        when(taskService.findPaginated(any(),anyInt(), anyInt())).thenReturn(Mono.just(Page.empty()));

        WebTestClient client = WebTestClient.bindToController(taskController).build();

        client.get()
                .uri("/task")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskDTO.class);
    }


    @Test
    public void controller_mustReturnNoContent_whenDeleteSucessfully() {
        String taskId = "any-id";
        when(taskService.deleteById(any())).thenReturn(Mono.empty());

        WebTestClient client = WebTestClient.bindToController(taskController).build();
        client.delete()
                .uri("/task/"+taskId)
                .exchange()
                .expectStatus()
                .isNoContent();


    }



    }
