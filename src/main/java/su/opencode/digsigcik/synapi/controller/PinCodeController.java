package su.opencode.digsigcik.synapi.controller;

import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.service.PinCodeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pincode")
public class PinCodeController {
    private final PinCodeRepository pinCodeRepository;

    public PinCodeController(PinCodeService pinCodeService, ESUserRepository esUserRepository,
                             PinCodeRepository pinCodeRepository) {
        this.pinCodeService = pinCodeService;
        this.esUserRepository = esUserRepository;
        this.pinCodeRepository = pinCodeRepository;
    }

    private final PinCodeService pinCodeService;
    private final ESUserRepository esUserRepository;

    @GetMapping("/")
    public List<PinCode> getAllPinCodes() {
        return pinCodeService.getAllPinCodes();
    }

    @PostMapping("/")
    public PinCode createPinCode(String login, String pinCode) {
        ESSUser essUser = esUserRepository.findESSUserByLogin(login);
        if (essUser != null) {
            PinCode pinCode1 = pinCodeRepository.getPinCodeByUser(essUser);
            if (pinCode1 != null) {
                pinCode1.setPinCode(pinCode);
                return pinCodeService.updatePinCode(pinCode1);
            } else {
                pinCode1 = PinCode.builder()
                        .user(essUser)
                        .pinCode(pinCode)
                        .build();
                return pinCodeService.createPinCode(pinCode1);
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @PutMapping("/")
    public PinCode updatePinCode(String pinCode) {
        PinCode pinCode1 = PinCode.builder()
                .pinCode(pinCode)
                .build();
        return pinCodeService.createPinCode(pinCode1);
    }

    @DeleteMapping("/")
    public void deletePinCode(Long id) {
        pinCodeService.deletePinCode(id);
    }

}