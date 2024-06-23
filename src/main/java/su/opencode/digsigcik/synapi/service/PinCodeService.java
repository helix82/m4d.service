package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.PinCode;

import java.util.List;

public interface PinCodeService {
    List<PinCode> getAllPinCodes();
    PinCode getPinCodeById(Long id);
    PinCode createPinCode(PinCode pinCode);
    PinCode updatePinCode(PinCode pinCode);
    void deletePinCode(Long id);
}