package br.com.udemy.tasks.controller;

import br.com.udemy.tasks.controller.converter.TaskDTOConverter;
import br.com.udemy.tasks.controller.converter.TaskInsertDTOConverter;
import br.com.udemy.tasks.controller.converter.TaskUpdateDTOConverter;
import br.com.udemy.tasks.controller.dto.TaskDTO;
import br.com.udemy.tasks.controller.dto.TaskInsertDTO;
import br.com.udemy.tasks.controller.dto.TaskUpdateDTO;
import br.com.udemy.tasks.model.TaskState;
import br.com.udemy.tasks.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final TaskDTOConverter converter;
    private final TaskInsertDTOConverter insertDTOConverter;
    private final TaskUpdateDTOConverter updateDTOConverter;

    public TaskController(TaskService taskService,
                          TaskDTOConverter converter,
                          TaskInsertDTOConverter insertDTOConverter,
                          TaskUpdateDTOConverter updateDTOConverter) {
        this.taskService = taskService;
        this.converter = converter;
        this.insertDTOConverter = insertDTOConverter;
        this.updateDTOConverter = updateDTOConverter;
    }

    @GetMapping
    public Mono<Page<TaskDTO>> getTasks(@RequestParam(required = false) String id,
                                        @RequestParam(required = false) String title,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false, defaultValue = "0") int priority,
                                        @RequestParam(required = false) TaskState state,
                                        @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        return taskService.findPaginated(converter.convert(id, title, description, priority, state), pageNumber, pageSize)
                .map(it -> it.map(converter::convert));
    }

    @PostMapping
    public Mono<TaskDTO> createTask(@RequestBody @Valid TaskInsertDTO taskDTO){
        return taskService.insert(insertDTOConverter.convert(taskDTO))
                .doOnNext(it -> LOGGER.info("Save task with id {}", it.getId()))
                .map(converter::convert);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id){
        return Mono.just(id)
                .doOnNext(it -> LOGGER.info("Deleting task with id {}", id))
                .flatMap(taskService::deleteById);
    }

    @PutMapping
    public Mono<TaskDTO> updateTask(@RequestBody @Valid TaskUpdateDTO dto){
        return taskService.update(updateDTOConverter.convert(dto))
                .doOnNext(it -> LOGGER.info("Update task with id {}", it.getId()))
                .map(converter::convert);
    }

    @PostMapping("/start")
    public Mono<TaskDTO> start(@RequestParam String id, @RequestParam String zipcode) {
        return taskService.start(id, zipcode)
                .map(converter::convert);
    }

    @PostMapping("/refresh/created")
    public Flux<TaskDTO> refreshCreated(){
        return taskService.refreshCreated()
                .map(converter::convert);
    }

    @PostMapping("/done")
    public Mono<List<TaskDTO>> done(@RequestBody List<String> ids) {
        return taskService.doneMany(ids)
                .map(converter::convertList);
    }

}
