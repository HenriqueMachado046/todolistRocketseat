package br.com.rocketseat.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.rocketseat.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * TaskController
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        
        UUID idUser = (UUID)request.getAttribute("idUser");
        taskModel.setIdUser(idUser);

        LocalDateTime currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isBefore(taskModel.getStartAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/término deve ser maior que a data atual");
        }

         if(taskModel.getStartAt().isBefore(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor que a data de término");
        }

        TaskModel task =this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body("Task criada com sucesso!");
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){
        //A mesma coisa da linha 30, apenas escrita em 1 linha para gastar menos espaço
        List<TaskModel> tasks = this.taskRepository.findByIdUser((UUID)request.getAttribute("idUser"));
        return tasks;
    }

    
    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){

        TaskModel task = this.taskRepository.findById(id).orElse(null);

        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task não existe");
        }else{
            if(!task.getIdUser().equals((UUID)request.getAttribute("idUser"))){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não tem permissão para alterar essa task");
            }

            Utils.copyNonNullProperties(taskModel, task);
       
            return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.save(task));
        }

       
    }


}