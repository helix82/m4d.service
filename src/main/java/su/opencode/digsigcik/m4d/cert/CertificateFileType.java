package su.opencode.digsigcik.m4d.cert;

public enum CertificateFileType {
    /**
     * Разные символы, есть символы перехода на новую строку.
     *
     * 0‚‹0‚s 	èÅÞ[úbÒ®/÷	|HW0
     *              <...>
     * 171102122425Z
     * 	*†H†÷
     */
    BINARY,

    /**
     * Готовый для парсинга формат.
     *
     * -----BEGIN CERTIFICATE-----
     *          <...>
     * -----END CERTIFICATE-----
     */
    FORMATTED,

    /**
     * MIIDRjCCAvOgAwIBAgITEgBkbQEAYQsGpDtI5wACAGRtATAKBggqhQMHAQEDAjB/
     * MSMwIQYJKoZIhvcNAQkBFhRzdXBwb3J0QGNyeXB0b3Byby5ydTELMAkGA1UEBhMC
     *              <...>
     * tXq3sSJ37/05XwwOLFDLtHkTo6PdddlDa/4=
     */
    BASE64_LINE_ESCAPED,

    /**
     * MIIDRjCCAvOgAwIBAgITEgBkbQEAYQsGpDtI5wACAGRtATAKBggqhQMHAQEDAjB/MSMw <...> 7/05XwwOLFDLtHkTo6PdddlDa/4=
     */
    BASE64_LINE_NOT_ESCAPED,

    /**
     * Certificate: <...>
     */
    PEM
}
