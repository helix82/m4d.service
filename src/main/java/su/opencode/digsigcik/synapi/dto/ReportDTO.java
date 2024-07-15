package su.opencode.digsigcik.synapi.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDTO {
    private List<Result> Results;
    private String ResultsReportBase64;

    @Getter
    public static class Result {
        private String SignedDocumentName;
        private String OriginalDocumentName;
        private String SignatureType;
        private String VerificationTime;
        private List<VerificationResult> VerificationResults;
        private String SignatureDocumentHash;
        private String CanExtractContent;

        @Getter
        public static class VerificationResult {
            private String Message;
            private boolean Result;
            private String SignerCertificate;
            private VerificationResult.SignerCertificateInfo SignerCertificateInfo;
            private List<Object> AdditionalCertificatesInfo;
            private VerificationResult.SignatureInfo SignatureInfo;
            private Object AdditionalCertificateResult;

            @Getter
            public static class SignerCertificateInfo {
                private String SubjectName;
                private String IssuerName;
                private String NotBefore;
                private long NotBeforeUTC;
                private String NotAfter;
                private long NotAfterUTC;
                private String SerialNumber;
                private String Thumbprint;
                private long PkNotBeforeUTC;
                private long PkNotAfterUTC;
                private String KeyIdentifier;

            }

            @Getter
            public static class SignatureInfo {
                private String CAdESType;
                private String LocalSigningTime;
                private long LocalSigningTimeUTC;

            }
        }
    }

    private String Message;
    private String MessageDetail;
    private String Error;
    private String ErrorDescription;
}
