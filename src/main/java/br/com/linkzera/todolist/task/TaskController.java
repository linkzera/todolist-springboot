package br.com.linkzera.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.linkzera.todolist.user.IUserRepository;
import br.com.linkzera.todolist.user.UserModel;
import br.com.linkzera.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @Autowired
  private IUserRepository userRepository;

  @PostMapping("/")
  public ResponseEntity<Object> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var task = this.taskRepository.findByTitle(taskModel.getTitle());
    var user = (UserModel) request.getAttribute("user");
    var currentDate = LocalDateTime.now();

    if (taskModel.getStartAt().isBefore(currentDate) || taskModel.getEndAt().isBefore(currentDate)) {
      return ResponseEntity.badRequest().body("Data de início/término não pode ser menor que a data atual!");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.badRequest().body("Data de início não pode ser maior que a data de término!");
    }

    if (task != null) {
      return ResponseEntity.badRequest().body("Tarefa já existe!");
    }

    var userFind = this.userRepository.findById(user.getId());

    if (userFind.isEmpty()) {
      return ResponseEntity.badRequest().body("Usuário não encontrado!");
    }

    taskModel.setUserId(userFind.get().getId());

    var taskCreated = this.taskRepository.save(taskModel);

    return ResponseEntity.ok(taskCreated);
  }

  @GetMapping("/")
  public ResponseEntity<Object> list(HttpServletRequest request) {
    var user = (UserModel) request.getAttribute("user");

    var tasks = this.taskRepository.findByUserId(user.getId());

    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> getById(@PathVariable("id") UUID id) {
    var task = this.taskRepository.findById(id);

    if (task.isEmpty()) {
      return ResponseEntity.badRequest().body("Tarefa não encontrada!");
    }

    return ResponseEntity.ok(task.get());
  }

  @PutMapping("/status/{id}")
  public ResponseEntity<Object> updateStatus(@PathVariable("id") UUID id, HttpServletRequest request) {
    var user = (UserModel) request.getAttribute("user");
    var task = this.taskRepository.findById(id);

    if (task.isEmpty()) {
      return ResponseEntity.badRequest().body("Tarefa não encontrada!");
    }

    if (task.get().getUserId().compareTo(user.getId()) != 0) {
      return ResponseEntity.badRequest().body("Tarefa não pertence ao usuário!");
    }

    task.get().setStatus(!task.get().isStatus());

    var taskUpdated = this.taskRepository.save(task.get());

    return ResponseEntity.ok(taskUpdated);
  }

  @PatchMapping("/{id}")
  public TaskModel update(@PathVariable("id") UUID id, @RequestBody TaskModel taskModel, HttpServletRequest request) {
    var task = this.taskRepository.findById(id).orElseThrow(null);
    Utils.copyNonNullProperties(taskModel, task);
    this.taskRepository.save(task);
    return task;
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") UUID id, HttpServletRequest request) {
    var user = (UserModel) request.getAttribute("user");
    var task = this.taskRepository.findById(id).orElse(null);

    if (task.getUserId().compareTo(user.getId()) != 0) {
      return ResponseEntity.badRequest().body("Tarefa não pertence ao usuário!");
    }

    this.taskRepository.deleteById(id);
    return ResponseEntity.ok("Tarefa deletada com sucesso!");
  }
}
