package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;

public interface CRLVerificationService {

    /**
     * Check against default CRLs.
     */
    ReportDTO.Result.VerificationResult verifyAgainstCRLs(Collection<X509Certificate> certificates);

    /**
     * Select CRLs manually.
     */
    ReportDTO.Result.VerificationResult verifyAgainstCRLs(Collection<X509Certificate> certificates, Collection<X509CRL> crls);
}
