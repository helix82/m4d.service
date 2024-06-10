package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.repository.RepresentativeRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

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
        return this.repository.save(representative);
    }

    @Override
    public Representative findByRepresentativeId(@NotNull Long id) {
        Representative representative = this.repository.findRepresentativeById(id);
        if (representative == null) {
            throw new MchdException(String.format("Representative %s", NOT_FOUND));
        }
        return representative;
    }

    @Override
    public Representative updateRepresentative(@NotNull Representative representative) {
        Long representativeId = representative.getId();
        if (representativeId == null) {
            throw new MchdException("Can`t update representative because id null!" );
        }

        Representative existRepresentative = this.repository.findRepresentativeById(representativeId);
        if (existRepresentative == null) {
            throw new MchdException(String.format("Can`t update. Representative with id=%d %s", representativeId, NOT_FOUND));
        }
        return this.repository.save(representative);
    }

    @Override
    public void deleteRepresentative(@NotNull Long id) {
        Representative representative = this.repository.findRepresentativeById(id);
        if (representative == null) {
            throw new MchdException(String.format("Can`t delete representative because representative %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
