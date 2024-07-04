package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.model.AcmeProvider;
import su.opencode.digsigcik.synapi.repository.ProviderRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class ProviderController {
    @Autowired
    private ProviderRepository providerRepository;

    @Operation(summary = "Получить список провайдеров",
            description = "Возвращает список провайдеров" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/providers")
    public ResponseEntity<List<AcmeProvider>> getAllProviders() {
        List<AcmeProvider> providers = providerRepository.findAll();
        return new ResponseEntity<>(providers, HttpStatus.OK);
    }

    @Operation(summary = "Получить указанного провайдера",
            description = "Возвращает указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/providers/{id}")
    public ResponseEntity<AcmeProvider> getProviderBylId(@PathVariable(value = "id") Long id) {
        AcmeProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + id));

        return new ResponseEntity<>(provider, HttpStatus.OK);
    }

    @Operation(summary = "Создать нового провайдера",
            description = "Создаёт нового провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно создано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/providers")
    public ResponseEntity<AcmeProvider> createProvider(@RequestBody AcmeProvider acmeProvider) {
        AcmeProvider _acmeProvider= new AcmeProvider(acmeProvider.getName(), acmeProvider.getDirectoryUrl());
        if(acmeProvider.getDomain()!=null)_acmeProvider.setDomain(acmeProvider.getDomain());
        if(acmeProvider.getIp()!=null)_acmeProvider.setIp(acmeProvider.getIp());
        if(acmeProvider.getDescription()!=null)_acmeProvider.setDescription(acmeProvider.getDescription());
        if(acmeProvider.getAutomatedRelease()!=null)_acmeProvider.setAutomatedRelease(acmeProvider.getAutomatedRelease());
        if(acmeProvider.getDeleted()!=null)_acmeProvider.setDeleted(acmeProvider.getDeleted());

        providerRepository.save(_acmeProvider);

        return new ResponseEntity<>(_acmeProvider, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить указанного провайдера",
            description = "Обновляет указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно обновлено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping("/providers/{id}")
    public ResponseEntity<AcmeProvider> updateProvider(@PathVariable("id") long id, @RequestBody AcmeProvider acmeProvider) {
        AcmeProvider _acmeProvider = providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + id));

        if(acmeProvider.getName()!=null)_acmeProvider.setName(acmeProvider.getName());
        if(acmeProvider.getDescription()!=null)_acmeProvider.setDescription(acmeProvider.getDescription());
        if(acmeProvider.getDomain()!=null)_acmeProvider.setDomain(acmeProvider.getDomain());
        if(acmeProvider.getIp()!=null)_acmeProvider.setIp(acmeProvider.getIp());
        if(acmeProvider.getDirectoryUrl()!=null)_acmeProvider.setDirectoryUrl(acmeProvider.getDirectoryUrl());
        if(acmeProvider.getAutomatedRelease()!=null)_acmeProvider.setAutomatedRelease(acmeProvider.getAutomatedRelease());
        if(acmeProvider.getDeleted()!=null)_acmeProvider.setDeleted(acmeProvider.getDeleted());

        return new ResponseEntity<>(providerRepository.save(_acmeProvider), HttpStatus.OK);
    }

    @Operation(summary = "Удалить указанного провайдера",
            description = "Удаляет указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/providers/{id}")
    public ResponseEntity<HttpStatus> deleteProvider(@PathVariable("id") long id) {
        providerRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
