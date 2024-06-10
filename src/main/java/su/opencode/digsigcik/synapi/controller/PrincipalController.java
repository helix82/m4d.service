package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.service.PrincipalService;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/principal")
@AllArgsConstructor
public class PrincipalController {
    private final String PRINCIPAL_IS_NULL = String.format("Principal is %s!", NULL);

    private final PrincipalService service;

    @GetMapping()
    public List<Principal> findAllPrincipal() {
        return service.findAllPrincipal();
    }

    @PostMapping("save")
    public ResponseEntity<?> savePrincipal(@RequestBody Principal principal) {
        Principal response;
        try {
            response = service.savePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Principal findByPrincipalId(@PathVariable Long id) {
        return service.findByPrincipalId(id);
    }

    @PutMapping("update")
    public ResponseEntity<?> updatePrincipal(@RequestBody Principal principal) {
        Principal response;
        try {
            response = service.updatePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePrincipal(@PathVariable Long id) {
        try {
            service.deletePrincipal(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        return ResponseEntity.ok().build();
    }
}
