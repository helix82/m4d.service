package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.RepresentativePassport;

import java.util.List;

public interface RepresentativePassportService {
    List<RepresentativePassport> findAllRepresentativePassport();
    RepresentativePassport saveRepresentativePassport(RepresentativePassport representativePassport);
    RepresentativePassport findByRepresentativePassportId(Long id);
    RepresentativePassport updateRepresentativePassport(RepresentativePassport representativePassport);
    void deleteRepresentativePassport(Long id);
}
