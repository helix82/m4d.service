package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDTO {
    @JsonProperty("Results")
    private List<Result> results;
    @JsonProperty("ResultsReportBase64")
    private String resultsReportBase64;

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class Result {
        @JsonProperty("SignedDocumentName")
        private String signedDocumentName;
        @JsonProperty("OriginalDocumentName")
        private String originalDocumentName;
        @JsonProperty("SignatureType")
        private String signatureType;
        @JsonProperty("VerificationTime")
        private String verificationTime;
        @JsonProperty("VerificationResults")
        private List<VerificationResult> verificationResults;
        @JsonProperty("SignatureDocumentHash")
        private String signatureDocumentHash;
        @JsonProperty("CanExtractContent")
        private String canExtractContent;

        @Data
        @AllArgsConstructor
        @Builder
        @NoArgsConstructor
        public static class VerificationResult {
            @JsonProperty("Message")
            private String message;
            @JsonProperty("Result")
            private boolean result;
            @JsonProperty("SignerCertificate")
            private String signerCertificate;
            @JsonProperty("SignerCertificateInfo")
            public VerificationResult.SignerCertificateInfo signerCertificateInfo;
            @JsonProperty("AdditionalCertificatesInfo")
            private List<Object> additionalCertificatesInfo;
            @JsonProperty("SignatureInfo")
            private VerificationResult.SignatureInfo signatureInfo;
            @JsonProperty("AdditionalCertificateResult")
            private Object additionalCertificateResult;

            @Data
            @AllArgsConstructor
            @Builder
            @NoArgsConstructor
            public static class SignerCertificateInfo {
                @JsonProperty("SubjectName")
                private String subjectName;
                @JsonProperty("IssuerName")
                private String issuerName;
                @JsonProperty("NotBefore")
                private String notBefore;
                @JsonProperty("NotBeforeUTC")
                private long notBeforeUTC;
                @JsonProperty("NotAfter")
                private String notAfter;
                @JsonProperty("NotAfterUTC")
                private long notAfterUTC;
                @JsonProperty("SerialNumber")
                private String serialNumber;
                @JsonProperty("Thumbprint")
                private String thumbprint;
                @JsonProperty("PkNotBeforeUTC")
                private long pkNotBeforeUTC;
                @JsonProperty("PkNotAfterUTC")
                private long pkNotAfterUTC;
                @JsonProperty("KeyIdentifier")
                private String keyIdentifier;
            }

            @Data
            @AllArgsConstructor
            @Builder
            @NoArgsConstructor
            public static class SignatureInfo {
                @JsonProperty("CAdESType")
                private String cAdESType;
                @JsonProperty("LocalSigningTime")
                private String localSigningTime;
                @JsonProperty("LocalSigningTimeUTC")
                private long localSigningTimeUTC;
            }
        }
    }

    @JsonProperty("Message")
    private String message;
    @JsonProperty("MessageDetail")
    private String messageDetail;
    @JsonProperty("Error")
    private String error;
    @JsonProperty("ErrorDescription")
    private String errorDescription;
}