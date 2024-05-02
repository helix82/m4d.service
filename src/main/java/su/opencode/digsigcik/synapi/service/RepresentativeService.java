package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.Representative;

import java.util.List;

public interface RepresentativeService {
    List<Representative> findAllRepresentative();
    Representative saveRepresentative(Representative representative);
    Representative findByRepresentativeId(Long id);
    Representative updateRepresentative(Representative representative);
    void deleteRepresentative(Long id);
}
