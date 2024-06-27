package su.opencode.digsigcik.synapi.service;

import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class PinCodeServiceImpl implements PinCodeService {
    private final PinCodeRepository pinCodeRepository;

    public PinCodeServiceImpl(PinCodeRepository pinCodeRepository) {
        this.pinCodeRepository = pinCodeRepository;
    }

    @Override
    public List<PinCode> getAllPinCodes() {
        return pinCodeRepository.findAll();
    }

    @Override
    public PinCode getPinCodeById(Long id) {
        return pinCodeRepository.getReferenceById(id);
    }

    @Override
    public PinCode createPinCode(PinCode pinCode) {
        return pinCodeRepository.save(pinCode);
    }

    @Override
    public PinCode updatePinCode(PinCode pinCode) {
        return pinCodeRepository.save(pinCode);
    }

    @Override
    @Transactional
    public void deleteByUser(UserProjection userProjection) {
        pinCodeRepository.deletePinCodeByUserProjection(userProjection);
    }

    // Реализация других методов...
}