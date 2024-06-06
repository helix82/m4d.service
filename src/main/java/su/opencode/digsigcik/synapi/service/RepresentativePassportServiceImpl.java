package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.RepresentativePassport;
import su.opencode.digsigcik.synapi.repository.RepresentativePassportRepository;

import java.util.List;

@Service
@Primary
public class RepresentativePassportServiceImpl implements RepresentativePassportService{
    private final RepresentativePassportRepository repository;

    @Autowired
    public RepresentativePassportServiceImpl(RepresentativePassportRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RepresentativePassport> findAllRepresentativePassport() {
        return this.repository.findAll();
    }

    @Override
    public RepresentativePassport saveRepresentativePassport(RepresentativePassport representativePassport) {
        return this.repository.save(representativePassport);
    }

    @Override
    public RepresentativePassport findByRepresentativePassportId(Long id) {
        return this.repository.findRepresentativePassportById(id);
    }

    @Override
    public RepresentativePassport updateRepresentativePassport(RepresentativePassport representativePassport) {
        return this.repository.save(representativePassport);
    }

    @Override
    public void deleteRepresentativePassport(Long id) {
        this.repository.deleteById(id);
    }
}
