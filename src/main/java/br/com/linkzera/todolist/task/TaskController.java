package br.com.linkzera.todolist.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.linkzera.todolist.user.IUserRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @Autowired
  private IUserRepository userRepository;
  
  @PostMapping("/")
  public ResponseEntity<Object> create(@RequestBody TaskModel taskModel) {
    var task = this.taskRepository.findByTitle(taskModel.getTitle());

    if (task != null) {
      return ResponseEntity.badRequest().body("Tarefa já existe!");
    }

    if(taskModel.getUserId() == null) return ResponseEntity.badRequest().body("Usuário não informado!");

    var user = this.userRepository.findById(taskModel.getUserId());

    if(user.isEmpty()) return ResponseEntity.badRequest().body("Usuário não encontrado!");
    
    var taskCreated = this.taskRepository.save(taskModel);
    return ResponseEntity.ok(taskCreated);
  }

  @GetMapping("/")
  public ResponseEntity<Object> list() {
    var tasks = this.taskRepository.findAll();
    return ResponseEntity.ok(tasks);
  }
}