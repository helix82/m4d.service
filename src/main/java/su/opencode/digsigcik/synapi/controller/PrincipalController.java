package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.service.PrincipalService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/principal")
@AllArgsConstructor
public class PrincipalController {
    private final PrincipalService service;

    @GetMapping()
    public List<Principal> findAllPrincipal() {
        return service.findAllPrincipal();
    }

    @PostMapping("save")
    public Principal savePrincipal(@RequestBody Principal principal) {
        return service.savePrincipal(principal);
    }

    @GetMapping("/{id}")
    public Principal findByPrincipalId(@PathVariable Long id) {
        return service.findByPrincipalId(id);
    }

    @PutMapping("update")
    public Principal updatePrincipal(@RequestBody Principal principal) {
        return service.updatePrincipal(principal);
    }

    @DeleteMapping("delete/{id}")
    public void deletePrincipal(@PathVariable Long id) {
        service.deletePrincipal(id);
    }
}
