package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.Representative;
import su.opencode.digsigcik.m4d.repository.RepresentativeRepository;
import su.opencode.digsigcik.m4d.service.RepresentativeService;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class RepresentativeServiceImpl implements RepresentativeService {
    private final RepresentativeRepository repository;

    @Override
    public List<Representative> findAllRepresentative() {
        return this.repository.findAll();
    }

    @Override
    public Representative saveRepresentative(@NotNull Representative representative) {
        String representativeInn = representative.getInn();
        if (representativeInn == null || representativeInn.isEmpty()) {
            throw new BadRequestException("Can`t create representative because inn null or empty! ");
        }
        return this.repository.save(representative);
    }

    @Override
    public Representative findByRepresentativeId(@NotNull Long id) {
        Representative representative = this.repository.findRepresentativeById(id);
        if (representative == null) {
            throw new M4dException("Representative not found!");
        }
        return representative;
    }

    @Override
    public Representative findRepresentativeByInn(@NotNull String inn) {
        Representative representative = this.repository.findRepresentativeByInn(inn);
        if (representative == null) {
            return null;
        }
        return representative;
    }

    @Override
    public Representative updateRepresentative(@NotNull Representative representative) {
        Long representativeId = representative.getId();
        if (representativeId == null) {
            throw new BadRequestException("Can`t update representative because id null!");
        }

        Representative existRepresentative = this.repository.findRepresentativeById(representativeId);
        if (existRepresentative == null) {
            throw new M4dException(String.format("Can`t update. Representative with id=%d not found!", representativeId));
        }
        return this.repository.save(representative);
    }

    @Override
    public void deleteRepresentative(@NotNull Long id) {
        Representative representative = this.repository.findRepresentativeById(id);
        if (representative == null) {
            throw new M4dException("Can`t delete representative because representative not found!");
        }
        this.repository.deleteById(id);
    }
}
