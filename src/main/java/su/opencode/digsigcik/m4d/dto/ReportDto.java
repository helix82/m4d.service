package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDto {
    @JsonProperty("Results")
    private List<Result> results;
    @JsonProperty("ResultsReportBase64")
    private String resultsReportBase64;

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        private String signature;
        private boolean attached;
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
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class VerificationResult {
            @JsonProperty("Message")
            private String message;
            @JsonProperty("Result")
            private boolean result;
            @JsonProperty("Details")
            private Details details;
            @JsonProperty("SignerCertificate")
            private String signerCertificate;
            @JsonProperty("SignerCertificateInfo")
            public VerificationResult.SignerCertificateInfo signerCertificateInfo;
            @JsonProperty("AdditionalCertificatesInfo")
            private List<AdditionalCertificatesInfo> additionalCertificatesInfo;
            @JsonProperty("SignatureInfo")
            private VerificationResult.SignatureInfo signatureInfo;
            @JsonProperty("AdditionalCertificateResult")
            private List<AdditionalCertificateResult> additionalCertificateResult;
            @JsonProperty("AdditionalInfo")
            private String additionalInfo;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Details {
                @JsonProperty("SignatureResult")
                private SignatureResult signatureResult;
                @JsonProperty("CertificateResult")
                private CertificateResult certificateResult;

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class SignatureResult {
                    @JsonProperty("Result")
                    private boolean result;
                    @JsonProperty("Error")
                    private ResultError error;
                }

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class CertificateResult {
                    @JsonProperty("Result")
                    private boolean result;
                    @JsonProperty("Errors")
                    private List<ResultError> errors;
                }

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class ResultError {
                    @JsonProperty("ErrorCode")
                    private String errorCode;
                    @JsonProperty("ErrorMessage")
                    private String errorMessage;
                }
            }

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AdditionalCertificateResult {
                @JsonProperty("AssemblyName")
                private String assemblyName;
                @JsonProperty("PluginDescription")
                private String pluginDescription;
                @JsonProperty("bResult")
                private boolean bResult;
                @JsonProperty("ErrorsList")
                private List<String> errorsList;
                @JsonProperty("AdditionalInfo")
                private List<String> additionalInfo;
                @JsonProperty("CertificateInfo")
                private CertificateInfo certificateInfo;

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class CertificateInfo {
                    @JsonProperty("OwnerType")
                    private String ownerType;
                }
            }

            @Data
            @AllArgsConstructor
            @Builder
            @NoArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AdditionalCertificatesInfo {
                @JsonProperty("SubjectName")
                private String subjectName;
                @JsonProperty("IssuerName")
                private String issuerName;
                @JsonProperty("NotBefore")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                private String notBefore;
                @JsonProperty("NotBeforeUTC")
                private long notBeforeUTC;
                @JsonProperty("NotAfter")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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

                @JsonProperty("CertDisplayName")
                private String certDisplayName;
                @JsonProperty("CertContent")
                private String certContent;
                @JsonProperty("CertificateId")
                private String certificateId;
                @JsonProperty("CertificateType")
                private String certificateType;
            }

            @Data
            @AllArgsConstructor
            @Builder
            @NoArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class SignerCertificateInfo {
                @JsonProperty("SubjectName")
                private String subjectName;
                @JsonProperty("IssuerName")
                private String issuerName;
                @JsonProperty("NotBefore")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                private String notBefore;
                @JsonProperty("NotBeforeUTC")
                private long notBeforeUTC;
                @JsonProperty("NotAfter")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class SignatureInfo {
                @JsonProperty("CAdESType")
                private String cAdESType;
                @JsonProperty("LocalSigningTime")
                private String localSigningTime;
                @JsonProperty("LocalSigningTimeUTC")
                private long localSigningTimeUTC;
                @JsonProperty("SigningTime")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                private String signingTime;
                @JsonProperty("SigningTimeUTC")
                private long signingTimeUTC;
                @JsonProperty("ArchiveSigningTime")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                private String archiveSigningTime;
                @JsonProperty("ArchiveSigningTimeUtc")
                private long archiveSigningTimeUtc;
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
    @JsonProperty("M4dVerificationReport")
    private ReportDto m4dVerificationReport;

    @JsonProperty("M4dFileVerificationReport")
    private ReportDto m4dFileVerificationReport;
    @JsonProperty("M4dServiceReport")
    private Map<String, Object> m4dServiceReport;
}