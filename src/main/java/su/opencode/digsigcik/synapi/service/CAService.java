package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.CA;

import java.util.List;

public interface CAService {
    List<CA> findAllCA();
    CA saveCA(CA ca);
    CA findCAByID(Long id);
    CA updateCA(CA ca);
    void deleteCA(Long id);
}
