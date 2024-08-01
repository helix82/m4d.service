package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.util.CertUtils;
import su.opencode.digsigcik.synapi.util.Constants;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CRLVerificationServiceOCode implements CRLVerificationService {

    private final RevokedCertificateService revokedCertificateService;

    @Override
    public ReportDTO.Result.VerificationResult verifyAgainstCRLs(Collection<X509Certificate> certificates) {

        return verifyAgainstCRLs(
                certificates,
                CertUtils.extractNativeCRLs(revokedCertificateService.findAllRevokedCertificate())
        );
    }

    @Override
    public ReportDTO.Result.VerificationResult verifyAgainstCRLs(Collection<X509Certificate> certificates, Collection<X509CRL> crls) {

        boolean noMatchWasFound = !checkIfSomeCertificatesPresentedInCRLs(
                certificates,
                crls
        );

        log.debug("Certificates in CRLs: " + (noMatchWasFound ? "No match" : "Matched in certain CRL"));

        return ReportDTO.Result.VerificationResult.builder()
                .result(noMatchWasFound)
                .message(noMatchWasFound ? Constants.CRL_SIGN_VERIFICATION_POSITIVE : Constants.CRL_SIGN_VERIFICATION_NEGATIVE)
                .build();
    }

    private boolean checkIfSomeCertificatesPresentedInCRLs(Collection<X509Certificate> certificates, Collection<X509CRL> crls) {

        log.debug("Checking against CRLs certificates: " +
                certificates
                        .stream()
                        .map(c -> Hex.toHexString(c.getSerialNumber().toByteArray()))
                        .collect(Collectors.joining(System.lineSeparator(), "[", "]"))
        );

        return crls.stream().anyMatch(crl -> {
            log.debug("CRL size: " + (Objects.isNull(crl.getRevokedCertificates()) ? 0 : crl.getRevokedCertificates().size()));
            return certificates.stream().anyMatch(crl::isRevoked);
        });
    }
}
