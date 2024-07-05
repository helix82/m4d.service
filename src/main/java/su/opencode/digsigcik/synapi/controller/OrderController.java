package su.opencode.digsigcik.synapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.dto.OrderDto;
import su.opencode.digsigcik.synapi.model.AcmeOrder;
import su.opencode.digsigcik.synapi.model.AcmeDomain;
import su.opencode.digsigcik.synapi.model.AcmeProvider;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;
import su.opencode.digsigcik.synapi.repository.OrderRepository;
import su.opencode.digsigcik.synapi.repository.ProviderRepository;
import su.opencode.digsigcik.synapi.service.AcmeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class OrderController {
    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private AcmeService acmeService;

    @Operation(summary = "Получить список заявок указанного домена",
            description = "Возвращает список заявок указанного домена" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/domains/{domainId}/orders")
    public ResponseEntity<List<Map>> getAllOrdersByDomainId(@PathVariable(value = "domainId") Long domainId) {
        if (!domainRepository.existsById(domainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeDomain with id = " + domainId);
        }
        List<AcmeOrder> orders = orderRepository.findByDomainId(domainId);
        return new ResponseEntity<>(processOrdersList(orders), HttpStatus.OK);
    }

    @Operation(summary = "Получить список заявок указанного провайдера",
            description = "Возвращает список заявок указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/providers/{providerId}/orders")
    public ResponseEntity<List<Map>> getAllOrdersByProviderId(@PathVariable(value = "providerId") Long providerId) {
        if (!providerRepository.existsById(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + providerId);
        }
        List<AcmeOrder> orders = orderRepository.findByProviderId(providerId);
        return new ResponseEntity<>(processOrdersList(orders), HttpStatus.OK);
    }

    private List<Map> processOrdersList(List<AcmeOrder> orders) {
        ArrayList<Map> list = new ArrayList<>();
        for(AcmeOrder order:orders) {
            if(!order.getDeleted()) {
                list.add(acmeService.orderInfo(order));
            }
        }
        return list;
    }

    @Operation(summary = "Получить список все заявок",
            description = "Возвращает список всех заявок" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/orders")
    public ResponseEntity<List<Map>> getAllOrders() {
        List<AcmeOrder> orders = orderRepository.findAll();
        return new ResponseEntity<>(processOrdersList(orders), HttpStatus.OK);
    }

    @Operation(summary = "Получить указанную заявку",
            description = "Возвращает указанную заявку" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/orders/{id}")
    public ResponseEntity<Map> getOrderBylId(@PathVariable(value = "id") Long id) {
        AcmeOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeOrder with id = " + id));

        return new ResponseEntity<>(acmeService.orderInfo(order), HttpStatus.OK);
    }

    @Operation(summary = "Сформировать заявку на выпуск нового сертификата",
            description = "Формирует заявку на выпуск нового сертификата" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Заявка успешно сформирована" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/orders",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map> createOrder(HttpSession httpSession, HttpServletRequest httpRequest,
                                            @RequestParam(value = "provider") Long providerId,
                                            @RequestParam(value = "domain") String domain,
                                            @RequestParam(value = "notAfter") String notAfter
    ) {
        Map data = httpRequest.getParameterMap();
        data.remove("provider");
        data.remove("domain");
        data.remove("notAfter");
        return acmeService.certificateOrder(httpSession, domain, notAfter, providerId, data);
    }

    @Operation(summary = "Сформировать заявку на выпуск нового сертификата (json)",
            description = "Формирует заявку на выпуск нового сертификата (json)" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Заявка успешно сформирована" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/orders",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> createOrderJson(HttpSession httpSession, @RequestBody String json ) {
        OrderDto order = null;
        try {
            order = new ObjectMapper().readValue(json, OrderDto.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid order format");
        }
        return acmeService.certificateOrder(httpSession, order.getDomain(), order.getNotAfter(), order.getProvider(), order.getData());
    }


    @Operation(summary = "Получить файл для прохождения челленджа по ранее сформированной заявке",
            description = "Получает файл для прохождения челленджа по ранее сформированной заявке" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Файл получен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/orders/{id}/download")
    public ResponseEntity<String> downloadOrder(HttpServletResponse httpResponse,
                                                        @PathVariable(value = "id") Long id
    ) {
        return acmeService.downloadChallengeFile(httpResponse,id);
    }

    @Operation(summary = "Выпустить новый сертификат по ранее сформированной заявке",
            description = "Выпускает новый сертификат по ранее сформированной заявке" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат получен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/orders/{id}/certificate")
    public ResponseEntity<Map> certificateRelease(HttpSession httpSession,
                                                   @PathVariable(value = "id") Long id
    ) {
        return acmeService.certificateRelease(httpSession,id);
    }


    @Operation(summary = "Удалить указанную заявку",
            description = "Удаляет указанную заявку" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable("id") long id) {
        AcmeOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeOrder with id = " + id));
        order.setDeleted(true);
        orderRepository.save(order);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Удалить заявки для указанного домена",
            description = "Удаляет заявки для указанного домена" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/domain/{domainId}/orders")
    public ResponseEntity<List<AcmeOrder>> deleteAllOrdersOfDomain(@PathVariable(value = "domainId") Long domainId) {
        if (!domainRepository.existsById(domainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found Domain with id = " + domainId);
        }
        List<AcmeOrder> orders = orderRepository.findByDomainId(domainId);
        for(AcmeOrder order: orders) {
            order.setDeleted(true);
            orderRepository.save(order);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
