package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.dto.CheckByGuidRequestDto;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dConfirm;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;

import java.io.IOException;

public interface PvvService {
    String sendM4dToRR(Authentication authentication, M4d m4d, M4dConfirm m4dConfirm) throws IOException;
    String sendM4dRevokeToRR(Authentication authentication, M4dRecall m4dRecall, M4dRecallConfirm m4dRecallConfirm) throws IOException;
    String sendRequestStatusM4dToRR(String guid);
    String sendRequestStatusM4dToRRByGuidAndInns(CheckByGuidRequestDto requestDto);
}
