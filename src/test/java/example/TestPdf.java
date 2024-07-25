package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.poi.xddf.usermodel.text.TextAlignment;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.awt.Color.*;

public class TestPdf {
    public static void main(String[] args) throws IOException, DocumentException {

        String strReportDTO = "{\n" +
                "    \"ResultsReportBase64\": null,\n" +
                "    \"Results\": [{\n" +
                "        \"OriginalDocumentName\": \"ON_EMCHD_20240718_270220da-575b-44a0-8490-8b91b9f0b8fe.xml\",\n" +
                "        \"CanExtractContent\": null,\n" +
                "        \"SignedDocumentName\": \"ON_EMCHD_20240718_270220da-575b-44a0-8490-8b91b9f0b8fe.xml.sig\"," +
                "\n" +
                "        \"SignatureType\": \"CMS\",\n" +
                "        \"VerificationTime\": \"2024-07-26T11:15:27.6831842+03:00\",\n" +
                "        \"SignatureDocumentHash\": \"t+JOUOVVFhZ1i/LU3lWCJOvu3iLhS5jKzbLq+hjyC3Y=\",\n" +
                "        \"VerificationResults\": [{\n" +
                "            \"SignerCertificateInfo\": {\n" +
                "                \"IssuerName\": \"CN=KEY_TEST_CA, O=TEST, C=RU\",\n" +
                "                \"SerialNumber\": \"6E1CC9DB776BC6B34A2E16B40190CFE0\",\n" +
                "                \"SubjectName\": \"CN=Прохоров Виктор, SN=Прохоров, G=Виктор, C=RU, S=Московская, " +
                "L=Москва\",\n" +
                "                \"NotBeforeUTC\": \"1721473888\",\n" +
                "                \"Thumbprint\": \"8B20BD22B468ED68B98C79E2D8B503DAF698D9B4\",\n" +
                "                \"PkNotBeforeUTC\": \"1721473887\",\n" +
                "                \"PkNotAfterUTC\": \"1816081887\",\n" +
                "                \"NotBefore\": \"2024-07-20T14:11:28\",\n" +
                "                \"NotAfter\": \"2040-02-01T15:13:56\",\n" +
                "                \"NotAfterUTC\": \"2211711236\"\n" +
                "            },\n" +
                "            \"AdditionalCertificatesInfo\": [],\n" +
                "            \"SignatureInfo\": {\n" +
                "                \"CAdESType\": \"BES\",\n" +
                "                \"LocalSigningTime\": \"2024-07-26T11:14:26\",\n" +
                "                \"LocalSigningTimeUTC\": \"1721981666\"\n" +
                "            },\n" +
                "            \"Message\": null,\n" +
                "            \"SignerCertificate\": " +
                "\"MIIE1TCCBIKgAwIBAgIQbhzJ23drxrNKLha0AZDP4DAKBggqhQMHAQEDAjA9MQswCQYDVQQGEwJSVTENMAsGA1UEChMEVEVTVDEfMB0GA1UEAx4WAEsARQBZAF8AVABFAFMAVABfAEMAQTAeFw0yNDA3MjAxMTExMjhaFw00MDAyMDExMjEzNTZaMIGdMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxHTAbBgNVBAgMFNCc0L7RgdC60L7QstGB0LrQsNGPMQswCQYDVQQGEwJSVTEVMBMGA1UEKgwM0JLQuNC60YLQvtGAMRkwFwYDVQQEDBDQn9GA0L7RhdC+0YDQvtCyMSYwJAYDVQQDDB3Qn9GA0L7RhdC+0YDQvtCyINCS0LjQutGC0L7RgDBmMB8GCCqFAwcBAQEBMBMGByqFAwICIwEGCCqFAwcBAQICA0MABECfyllzVJbtDfBMZfxIkczLiGRhVm12AV+SXCEU/t2ryJ8emrJPIbxo/Jb6ar1kfGC3e5J7i7+mmvkV0kACvxXVo4IC9DCCAvAwFgYDVR0lAQH/BAwwCgYIKwYBBQUHAwgwXQYIKwYBBQUHAQEEUTBPMCcGCCsGAQUFBzABhhtodHRwOi8va2V5bGFiL29jc3Avb2NzcC5zcmYwJAYIKwYBBQUHMAKGGGh0dHA6Ly9rZXlsYWIvYWlhL2NhLmNydDAOBgNVHQ8BAf8EBAMCBsAwHQYDVR0gBBYwFDAIBgYqhQNkcQEwCAYGKoUDZHECMIH6BgUqhQNkcASB8DCB7Qwj0KHQmtCX0Jgg0JrRgNC40L/RgtC+0J/RgNC+IENTUCA1LjAMF9Ca0YDQuNC/0YLQvtCf0YDQviDQo9CmDE/QodC10YDRgtC40YTQuNC60LDRgiDRgdC+0L7RgtCy0LXRgtGB0YLQstC40Y8g0KTQodCRINCg0L7RgdGB0LjQuCDQvdCwINCh0JrQl9CYDFzQodC10YDRgtC40YTQuNC60LDRgiDRgdC+0L7RgtCy0LXRgtGB0YLQstC40Y8g0KTQodCRINCg0L7RgdGB0LjQuCDQvdCwINGB0YDQtdC00YHRgtCy0LAg0KPQpjAqBgUqhQNkbwQhDB/QodCa0JfQmCDQmtGA0LjQv9GC0L7Qn9GA0L4gQ1NQMCsGA1UdEAQkMCKADzIwMjQwNzIwMTExMTI3WoEPMjAyNzA3MjAxMTExMjdaMAwGBSqFA2RyBAMCAQAwTwYDVR0fBEgwRjBEoEKgQIY+aHR0cDovL2tleWxhYi9jZHAvRTA2Qjg5RTNFMzNFOEUwN0Y5OTE3QkRFMjI0RTVGNEQ3QTAyQjJGQS5jcmwwdAYDVR0jBG0wa4AU4GuJ4+M+jgf5kXveIk5fTXoCsvqhQaQ/MD0xCzAJBgNVBAYTAlJVMQ0wCwYDVQQKEwRURVNUMR8wHQYDVQQDHhYASwBFAFkAXwBUAEUAUwBUAF8AQwBBghBJ3Hqfv03ZrE3i4N0/EM+zMB0GA1UdDgQWBBRBrUIYnEq4UkTC6s2sMet4RAV/pzAKBggqhQMHAQEDAgNBAFgb/40kcyZPxR5ksgSugRYn8J03Ze715RkH7AAJyuIL942dNdNN+ZAm4hYAFrlzl8Kiv7h3YSqjSEUUfR0bL2g=\",\n" +
                "            \"AdditionalCertificateResult\": null,\n" +
                "            \"Result\": true\n" +
                "        }]\n" +
                "    }]\n" +
                "}";


        ReportDTO reportDTO = new ObjectMapper().readValue(strReportDTO, ReportDTO.class);
        String fileName = "protocol.pdf";


        // Создаем выходной поток файла
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        // Записываем байты PDF-документа в файл
        fileOutputStream.write(createPdf(reportDTO));

        // Закрываем выходной поток файла
        fileOutputStream.close();

        System.out.println("PDF-документ сохранен в файл: " + fileName);
    }


    public static byte[] createPdf(ReportDTO reportDTO) throws DocumentException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        // Add fonts
        BaseFont baseFont = BaseFont.createFont("/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontHeader = new Font(baseFont, 16, Font.BOLD);
        Font fontSection = new Font(baseFont, 14, Font.BOLD);
        Font fontContent = new Font(baseFont, 10);

        // Add title
        Paragraph title = new Paragraph("ПРОТОКОЛ ПРОВЕРКИ ЭЛЕКТРОННОЙ ПОДПИСИ", fontHeader);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        // Create unified table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100.0f);
        table.setWidths(new float[]{3.0f, 7.0f});
        table.setSpacingBefore(10);

        // Adding sections and data
        boolean valid = reportDTO.getResults().get(0).getVerificationResults().get(0).isResult();
        addSection(table, "Результат проверки", fontSection, fontContent);
        addRow(table,  valid ? "Подпись действительна" : "Подпись недействительна",
                valid, fontContent);

        addSection(table, "Сведения о файле", fontSection, fontContent);
        addRow(table, "Имя файла:", reportDTO.getResults().get(0).getOriginalDocumentName(), fontContent);
        addRow(table, "Исходное имя файла:", reportDTO.getResults().get(0).getSignedDocumentName(), fontContent);
        addRow(table, "Хэш файла подписи:", reportDTO.getResults().get(0).getSignatureDocumentHash(), fontContent);

        addSection(table, "Сведения о подписи", fontSection, fontContent);
        addRow(table, "Тип подписи:", reportDTO.getResults().get(0).getSignatureType(), fontContent);
        addRow(table, "Время проверки:", reportDTO.getResults().get(0).getVerificationTime(), fontContent);
        addRow(table, "Ошибки:", "N/A", fontContent);

        addSection(table, "Сведения о сертификате подписи", fontSection, fontContent);
        ReportDTO.Result.VerificationResult.SignerCertificateInfo certInfo = reportDTO.getResults().get(0).getVerificationResults().get(0).getSignerCertificateInfo();
        addRow(table, "Субъект:", certInfo.getSubjectName(), fontContent);
        addRow(table, "Издатель:", certInfo.getIssuerName(), fontContent);
        addRow(table, "Действителен до:", certInfo.getNotAfter(), fontContent);
        addRow(table, "Закрытый ключ действителен до:", String.valueOf(certInfo.getPkNotAfterUTC()), fontContent);
        addRow(table, "Серийный номер:", certInfo.getSerialNumber(), fontContent);
        addRow(table, "Отпечаток:", certInfo.getThumbprint(), fontContent);

        addSection(table, "Доп. данные", fontSection, fontContent);
        addRow(table, "Время подписи:", reportDTO.getResults().get(0).getVerificationTime(), fontContent);
        addRow(table, "Формат подписи CAdES:", reportDTO.getResults().get(0).getSignatureType(), fontContent);

        // Add the table to the document
        document.add(table);

        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static void addSection(PdfPTable table, String sectionTitle, Font fontSection, Font fontContent) {
        PdfPCell sectionCell = new PdfPCell(new Phrase(sectionTitle, fontSection));
        sectionCell.setColspan(2);
        sectionCell.setBorder(Rectangle.NO_BORDER);
        sectionCell.setPadding(10f);
        sectionCell.setBackgroundColor(LIGHT_GRAY);
        sectionCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(sectionCell);
    }

    private static void addRow(PdfPTable table, String label, String value, Font fontContent) {
        PdfPCell cell;

        cell = new PdfPCell(new Phrase(label, fontContent));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10f);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(value, fontContent));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10f);
        table.addCell(cell);
    }
    private static void addRow(PdfPTable table, String label, boolean valid, Font fontContent) {
        PdfPCell cell;
        cell = new PdfPCell(new Phrase(label, fontContent));
        if (valid) {
            cell.setBackgroundColor(GREEN);
        } else {
            cell.setBackgroundColor(RED);
        }
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10f);
        table.addCell(cell);
    }
}
