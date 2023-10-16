package br.com.linkzera.todolist.task;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITaskRepository extends JpaRepository<TaskModel, UUID> {
  TaskModel findByTitle(String title);

  List<TaskModel> findByUserId(UUID userId);
}
