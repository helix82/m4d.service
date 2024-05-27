package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.CA;
import su.opencode.digsigcik.synapi.repository.CARepository;

import java.util.List;

@Service
@Primary
public class CAServiceImpl implements CAService {
    private final CARepository caRepository;

    @Autowired
    public CAServiceImpl(CARepository caRepository) {
        this.caRepository = caRepository;
    }

    @Override
    public List<CA> findAllCA() {
        return this.caRepository.findAll();
    }

    @Override
    public CA saveCA(CA ca) {
        return this.caRepository.save(ca);
    }

    @Override
    public CA findCAByID(Long id) {
        return this.caRepository.findCAById(id);
    }

    @Override
    public CA updateCA(CA ca) {
        return caRepository.save(ca);
    }

    @Override
    public void deleteCA(Long id) {
        this.caRepository.deleteById(id);
    }

}
