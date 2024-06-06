package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.RepresentativePassport;
import su.opencode.digsigcik.synapi.service.RepresentativePassportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/representative_passport")
@AllArgsConstructor
public class RepresentativePassportController {
    private final RepresentativePassportService service;

    @GetMapping()
    public List<RepresentativePassport> findAllRepresentativePassport() {
        return service.findAllRepresentativePassport();
    }

    @PostMapping("save")
    public RepresentativePassport saveRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        return service.saveRepresentativePassport(representativePassport);
    }

    @GetMapping("/{id}")
    public RepresentativePassport findByRepresentativePassportId(@PathVariable Long id) {
        return service.findByRepresentativePassportId(id);
    }

    @PutMapping("update")
    public RepresentativePassport updateRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        return service.updateRepresentativePassport(representativePassport);
    }

    @DeleteMapping("delete/{id}")
    public void deleteRepresentativePassport(@PathVariable Long id) {
        service.deleteRepresentativePassport(id);
    }
}
