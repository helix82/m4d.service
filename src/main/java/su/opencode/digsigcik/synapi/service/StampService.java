package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StampService {

    static Color HexToColor(String hex) {
        hex = hex.replace("#", "");
        switch (hex.length()) {
            case 6:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16));
            case 8:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        Integer.valueOf(hex.substring(6, 8), 16));
        }
        return null;
    }

    int defaultLineWidth = 3;
    int defaultPadding = 0;
    int defaultMargin = 0;
    int defaultRound = 10;
    int defaultWidth = 0;
    int defaultHeight = 0;
    Color defaultColor = new Color(0,0,0,255);
    String defaultFamily = "TimesRoman";
    String defaultStyle = "bold"; //Font.BOLD
    int defaultSize= 14;
    boolean defaultInverse= false;
    BufferedImage tmp = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    class Box {
        int width;
        int height;

        Box(int w, int h) {
            this.width = w;
            this.height = h;
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getHeight() {
            return height;
        }

        void setHeight(int height) {
            this.height = height;
        }
    }


    byte[] doStamp(String documentUrl, String stampUrl, Integer page, Integer offsetX, Integer offsetY, Integer scale);

    byte[] doStamp(String documentUrl, InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale);

    byte[] doStampDocx(InputStream document, String stampUrl, Integer page, Integer offsetX, Integer offsetY, Integer scale);

    byte[] doStampPdf(InputStream document, String stampUrl, Integer page, Integer offsetX, Integer offsetY, Integer scale);

    byte[] doStampDocx(InputStream document, InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale);

    byte[] doStampPdf(InputStream document, InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale);

    BufferedImage doPreview(byte[] pdfData, Integer page);


    BufferedImage makeStampImage(String json) throws JsonProcessingException;
    byte[] createPdfPreviewWithStamp(MultipartFile sourceFile,
                                     @NotNull ContainerInputInfoDto containerInputInfo) throws IOException;
    byte[] createPngPreviewWithStamp(MultipartFile sourceFile,
                                     @NotNull ContainerInputInfoDto containerInputInfo) throws IOException;

}
