package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.service.RepresentativeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/representative")
@AllArgsConstructor
public class RepresentativeController {
    private final RepresentativeService service;

    @GetMapping()
    public List<Representative> findAllRepresentative() {
        return service.findAllRepresentative();
    }

    @PostMapping("save")
    public Representative saveRepresentative(@RequestBody Representative representative) {
        return service.saveRepresentative(representative);
    }

    @GetMapping("/{id}")
    public Representative findByRepresentativeId(@PathVariable Long id) {
        return service.findByRepresentativeId(id);
    }

    @PutMapping("update")
    public Representative updateRepresentative(@RequestBody Representative representative) {
        return service.updateRepresentative(representative);
    }

    @DeleteMapping("delete/{id}")
    public void deleteRepresentative(@PathVariable Long id) {
        service.deleteRepresentative(id);
    }
}
