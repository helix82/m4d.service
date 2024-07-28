package su.opencode.digsigcik.synapi.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.experimental.UtilityClass;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.awt.Color.*;

@UtilityClass
public class ReportUtils {
    public static byte[] createPdf(ReportDTO reportDTO) throws DocumentException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        // Add fonts
        BaseFont baseFont = BaseFont.createFont("/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        com.lowagie.text.Font fontHeader = new com.lowagie.text.Font(baseFont, 16, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font fontSection = new com.lowagie.text.Font(baseFont, 14, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font fontContent = new com.lowagie.text.Font(baseFont, 10);

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
        addSection(table, "Результат проверки", fontSection);
        if (reportDTO.getError() != null) {
            addRow(table, reportDTO.getError(), false, fontContent);
            addRow(table, reportDTO.getErrorDescription(), false, fontContent);
        } else {
            // Adding sections and data
            boolean valid = reportDTO.getResults()
                    .get(0)
                    .getVerificationResults()
                    .get(0)
                    .isResult();
            addRow(table, valid ? "Подпись действительна" : "Подпись недействительна",
                    valid, fontContent);
            if (!valid) {
                addRow(table, reportDTO.getResults()
                                .get(0)
                                .getVerificationResults()
                                .get(0)
                                .getMessage(),
                        false, fontContent);
            }

            addSection(table, "Сведения о файле", fontSection);
            addRow(table, "Имя файла:", reportDTO.getResults()
                    .get(0)
                    .getOriginalDocumentName(), fontContent);
            addRow(table, "Исходное имя файла:", reportDTO.getResults()
                    .get(0)
                    .getSignedDocumentName(), fontContent);
            addRow(table, "Хэш файла подписи:", reportDTO.getResults()
                    .get(0)
                    .getSignatureDocumentHash(), fontContent);

            addSection(table, "Сведения о подписи", fontSection);
            addRow(table, "Тип подписи:", reportDTO.getResults()
                    .get(0)
                    .getSignatureType(), fontContent);
            addRow(table, "Время проверки:", reportDTO.getResults()
                    .get(0)
                    .getVerificationTime(), fontContent);
            addRow(table, "Ошибки:", "N/A", fontContent);

            addSection(table, "Сведения о сертификате подписи", fontSection);
            ReportDTO.Result.VerificationResult.SignerCertificateInfo certInfo = reportDTO.getResults()
                    .get(0)
                    .getVerificationResults()
                    .get(0)
                    .getSignerCertificateInfo();
            addRow(table, "Субъект:", certInfo.getSubjectName(), fontContent);
            addRow(table, "Издатель:", certInfo.getIssuerName(), fontContent);
            addRow(table, "Действителен до:", certInfo.getNotAfter(), fontContent);
            addRow(table, "Закрытый ключ действителен до:", String.valueOf(certInfo.getPkNotAfterUTC()), fontContent);
            addRow(table, "Серийный номер:", certInfo.getSerialNumber(), fontContent);
            addRow(table, "Отпечаток:", certInfo.getThumbprint(), fontContent);

            addSection(table, "Доп. данные", fontSection);
            addRow(table, "Время подписи:", reportDTO.getResults()
                    .get(0)
                    .getVerificationTime(), fontContent);
            addRow(table, "Формат подписи CAdES:", reportDTO.getResults()
                    .get(0)
                    .getVerificationResults()
                    .get(0)
                    .getSignatureInfo()
                    .getCAdESType(), fontContent);
        }
        // Add the table to the document
        document.add(table);

        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static void addSection(PdfPTable table, String sectionTitle, com.lowagie.text.Font fontSection) {
        PdfPCell sectionCell = new PdfPCell(new Phrase(sectionTitle, fontSection));
        sectionCell.setColspan(2);
        sectionCell.setBorder(Rectangle.NO_BORDER);
        sectionCell.setPadding(10f);
        sectionCell.setBackgroundColor(LIGHT_GRAY);
        sectionCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(sectionCell);
    }

    private static void addRow(PdfPTable table, String label, String value, com.lowagie.text.Font fontContent) {
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

    private static void addRow(PdfPTable table, String label, boolean valid, com.lowagie.text.Font fontContent) {
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
