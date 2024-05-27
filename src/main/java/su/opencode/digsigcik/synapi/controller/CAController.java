package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.CA;
import su.opencode.digsigcik.synapi.service.CAService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ca")
@AllArgsConstructor
public class CAController {
    private final CAService service;

    @GetMapping()
    public List<CA> findAllCA() {
        return service.findAllCA();
    }

    @PostMapping("save")
    public CA saveCA(@RequestBody CA ca) {
        return service.saveCA(ca);
    }

    @GetMapping("/{id}")
    public CA findByCAId(@PathVariable Long id) {
        return service.findCAByID(id);
    }

    @PutMapping("update")
    public CA updateCA(@RequestBody CA ca) {
        return service.updateCA(ca);
    }

    @DeleteMapping("delete/{id}")
    public void deleteCA(@PathVariable Long id) {
        service.deleteCA(id);
    }
}
