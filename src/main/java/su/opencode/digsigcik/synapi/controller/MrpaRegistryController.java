package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mrpa_registry")
@AllArgsConstructor
public class MrpaRegistryController {
    private final MrpaRegistryService service;

    @GetMapping()
    public List<MrpaRegistry> findAllMrpaRegistry() {
        return service.findAllMrpaRegistry();
    }

    @PostMapping("save")
    public MrpaRegistry saveMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        return service.saveMrpaRegistry(mrpaRegistry);
    }

    @GetMapping("/{id}")
    public MrpaRegistry findByMrpaRegistryId(@PathVariable Long id) {
        return service.findByMrpaRegistryId(id);
    }

    @PutMapping("update")
    public MrpaRegistry updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        return service.updateMrpaRegistry(mrpaRegistry);
    }

    @DeleteMapping("delete/{id}")
    public void deleteMrpaRegistry(@PathVariable Long id) {
        service.deleteMrpaRegistry(id);
    }

}
