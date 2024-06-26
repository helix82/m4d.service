package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VerificationCenterEnum {
    FEDERAL_TAX_SERVICE("Федеральная налоговая служба"),
    FEDERAL_TREASURY("Федеральное казначейство"),
    FEDERAL_CENTER_INFORMATIZATION("Федеральный центр информатизации");

    private final String title;
}
