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
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.service.OrganisationService;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/organisation")
@AllArgsConstructor
public class OrganisationController {
    private final String ORGANISATION_IS_NULL = String.format("Organisation is %s!", NULL);

    private final OrganisationService service;

    @GetMapping()
    public List<Organisation> findAllOrganisation() {
        return service.findAllOrganisation();
    }

    @PostMapping("save")
    public ResponseEntity<?> saveOrganisation(@RequestBody Organisation organisation) {
        Organisation response;
        try {
            response = service.saveOrganisation(organisation);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Organisation findByOrganisationId(@PathVariable Long id) {
        return service.findByOrganisationId(id);
    }

    @PutMapping("update")
    public ResponseEntity<?> updateOrganisation(@RequestBody Organisation organisation) {
        Organisation response;
        try {
            response = service.updateOrganisation(organisation);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteOrganisation(@PathVariable Long id) {
        try {
            service.deleteOrganisation(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        return ResponseEntity.ok().build();
    }
}
