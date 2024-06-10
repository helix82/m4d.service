package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.service.RepresentativeService;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/representative")
@AllArgsConstructor
public class RepresentativeController {
    private final String REPRESENTATIVE_IS_NULL = String.format("Representative is %s!", NULL);
    private final RepresentativeService service;

    @GetMapping()
    public List<Representative> findAllRepresentative() {
        return service.findAllRepresentative();
    }

    @PostMapping("save")
    public ResponseEntity<?> saveRepresentative(@RequestBody Representative representative) {
        Representative response;
        try {
            response = service.saveRepresentative(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Representative findByRepresentativeId(@PathVariable Long id) {
        return service.findByRepresentativeId(id);
    }

    @PutMapping("update")
    public ResponseEntity<?> updateRepresentative(@RequestBody Representative representative) {
        Representative response;
        try {
            response = service.updateRepresentative(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteRepresentative(@PathVariable Long id) {
        try {
            service.deleteRepresentative(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        return ResponseEntity.ok().build();
    }
}
