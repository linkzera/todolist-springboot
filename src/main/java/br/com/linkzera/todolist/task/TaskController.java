package br.com.linkzera.todolist.task;

import java.lang.reflect.Field;
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
  public ResponseEntity<Object> update(@PathVariable("id") UUID id, @RequestBody TaskModel taskModel,
      HttpServletRequest request) {
    var user = (UserModel) request.getAttribute("user");
    var task = this.taskRepository.findById(id);

    if (task.isEmpty()) {
      return ResponseEntity.badRequest().body("Tarefa não encontrada!");
    }

    if (task.get().getUserId().compareTo(user.getId()) != 0) {
      return ResponseEntity.badRequest().body("Tarefa não pertence ao usuário!");
    }

    Class<?> taskClass = task.get().getClass();

    for (Field field : taskModel.getClass().getDeclaredFields()) {
      try {
        field.setAccessible(true);
        Object value = field.get(taskModel);

        if (value != null && taskClass.getDeclaredField(field.getName()) != null) {
          Field taskField = taskClass.getDeclaredField(field.getName());
          taskField.setAccessible(true);
          taskField.set(task.get(), value);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    var taskUpdated = this.taskRepository.save(task.get());

    return ResponseEntity.ok(taskUpdated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") UUID id, HttpServletRequest request) {
    var user = (UserModel) request.getAttribute("user");
    var task = this.taskRepository.findById(id);

    if (task.isEmpty()) {
      return ResponseEntity.badRequest().body("Tarefa não encontrada!");
    }

    if (task.get().getUserId().compareTo(user.getId()) != 0) {
      return ResponseEntity.badRequest().body("Tarefa não pertence ao usuário!");
    }

    this.taskRepository.deleteById(id);
    return ResponseEntity.ok("Tarefa deletada com sucesso!");
  }
}
