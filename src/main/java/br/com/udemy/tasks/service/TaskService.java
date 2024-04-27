package br.com.udemy.tasks.service;

import br.com.udemy.tasks.exception.TaskNotFoundException;
import br.com.udemy.tasks.messaging.TaskNotificationProducer;
import br.com.udemy.tasks.model.Address;
import br.com.udemy.tasks.model.Task;
import br.com.udemy.tasks.repository.TaskRepository;
import br.com.udemy.tasks.repository.custom.TaskCustomRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final TaskCustomRepository taskCustomRepository;
    private final AddressService addressService;
    private final TaskNotificationProducer taskNotificationProducer;


    public TaskService(TaskRepository taskRepository, TaskCustomRepository taskCustomRepository, AddressService addressService, TaskNotificationProducer taskNotificationProducer) {
        this.taskRepository = taskRepository;
        this.taskCustomRepository = taskCustomRepository;
        this.addressService = addressService;
        this.taskNotificationProducer = taskNotificationProducer;
    }

    public Mono<Task> insert(Task task){
        return Mono.just(task)
                .map(Task::insert)
                .flatMap(this::save)
                .doOnError(error -> LOGGER.error("Error during save task. Title: {}", task.getTitle(), error));
    }

    public  Mono<Page<Task>> findPaginated(Task task, Integer pageNumber, Integer pageSize){
        return taskCustomRepository.findPaginated(task, pageNumber, pageSize);

    }

    public Mono<Void> deleteById(String id){
        return taskRepository.deleteById(id);
    }

    public Mono<Task> update(Task task) {
        return taskRepository.findById(task.getId())
                .map(task::update)
                .flatMap(taskRepository::save)
                .switchIfEmpty(Mono.error(TaskNotFoundException::new))
                .doOnError(error -> LOGGER.error("Erro during update task with id {}. Message: {}", task.getId(), error.getMessage()));
     }


     public Mono<Task> start(String id, String zipCode){
        return taskRepository.findById(id)
                .zipWhen(it -> addressService.getAdress(zipCode))
                .flatMap(it -> updateAdress(it.getT1(), it.getT2()))
                .map(Task::start)
                .flatMap(taskRepository::save)
                .flatMap(taskNotificationProducer::sendNotification)
                .switchIfEmpty(Mono.error(TaskNotFoundException::new))
                .doOnError(error -> LOGGER.error("Error on start task. ID: {}", id));
     }

    public Mono<Task> done(Task task) {
        return Mono.just(task)
                .doOnNext(it -> LOGGER.info("Finish task. ID: {}", task.getId()))
                .map(Task::done)
                .flatMap(taskRepository::save);
    }



    public Flux<Task> refreshCreated() {
        return taskRepository.findAll()
                .filter(Task::createdIsEmpty)
                .map(Task::createdNow)
                .flatMap(taskRepository::save);
    }

    private Mono<Task> updateAdress(Task task, Address address){
        return Mono.just(task)
                .map(it -> task.updateAddress(address));
    }

    private Mono<Task> save(Task task){
        return Mono.just(task)
                .doOnNext(it -> LOGGER.info("Save task with title {}", it.getTitle()))
                .flatMap(taskRepository::save);
    }


    public Mono<List<Task>> doneMany(List<String> ids) {
        return Flux.fromIterable(ids)
                .flatMap(id -> taskRepository.findById(id)
                        .map(Task::done)
                        .flatMap(taskRepository::save)
                        .doOnNext(it -> LOGGER.info("Done task. ID: {}", it.getId()))
                ).collectList();
    }

    @PostConstruct
        private void scheduleDoneOlderTaks() {
        Mono.delay(Duration.ofSeconds(5))
                .doOnNext(it -> LOGGER.info("Starting task monitoring"))
                .subscribe();

        Flux.interval(Duration.ofDays(1))
                .flatMap(it -> doneOlderTaks())
                .filter(it -> it > 0)
                .doOnNext(tasks -> LOGGER.info("{} tak(s) completed after begin active for over 7 days.", tasks))
                .subscribe();
    }


    private Mono<Long> doneOlderTaks(){
        return taskCustomRepository.updateStateToDoneForOlderTaks(LocalDate.now().minusDays(7));
    }


}
