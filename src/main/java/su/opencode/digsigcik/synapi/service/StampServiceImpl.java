package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.StampData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

@Service
@RequiredArgsConstructor
public class StampServiceImpl implements StampService {

    @Override
    public byte[] doStamp(String documentUrl, String stampUrl, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        InputStream stampStream = null;
        try {
            stampStream = new URL(stampUrl).openStream();

            return doStamp(documentUrl, stampStream, page, offsetX, offsetY, scale);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stampStream!=null)stampStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public byte[] doStamp(String documentUrl, InputStream stampStream, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        InputStream documentStream = null;
        try {
            documentStream = new URL(documentUrl).openStream();

            if(documentUrl.endsWith(".docx")){
                return doStampDocx(documentStream, stampStream, page, offsetX, offsetY, scale);
            } else {
                return doStampPdf(documentStream, stampStream, page, offsetX, offsetY, scale);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(documentStream!=null)documentStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public byte[] doStampDocx(InputStream document, String stampUrl, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        InputStream stampStream = null;
        try {
            stampStream = new URL(stampUrl).openStream();
            return doStampDocx(document, stampStream, page, offsetX, offsetY, scale);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stampStream!=null)stampStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public byte[] doStampPdf(InputStream document, String stampUrl, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        InputStream stampStream = null;
        try {
            stampStream = new URL(stampUrl).openStream();
            return doStampPdf(document, stampStream, page, offsetX, offsetY, scale);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stampStream!=null)stampStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public byte[] doStampDocx(InputStream document, InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(new XWPFDocument(document), outputStream, options);
            InputStream pdfStream = new ByteArrayInputStream(outputStream.toByteArray());
            return doStampPdf(pdfStream, stamp, page, offsetX, offsetY, scale);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] doStampPdf(InputStream document, InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfReader reader = new PdfReader(document);
            Integer pagesCount = reader.getNumberOfPages();
            Integer stampPage = pagesCount;
            if(page!=null){
                if(page>0&&page<pagesCount){
                    stampPage = page;
                }
                if(page>-pagesCount&&page<0){
                    stampPage = pagesCount + page + 1;
                }
            }
            PdfStamper stamper = new PdfStamper(reader, baos, '\0', true);

            Image img = Image.getInstance(ImageIO.read(stamp), null);
            if(scale != null )
                img.scalePercent(scale);

            float w = img.getScaledWidth();
            float h = img.getScaledHeight();

            Rectangle pagesize = reader.getPageSize(stampPage);
            int oX = (int) (pagesize.getWidth() - w), oY=0;
            if(offsetX!=null)oX = ((offsetX<0)?((int)(pagesize.getWidth() - w) ):0) + offsetX;
            if(offsetY!=null)oY = ((offsetY<0)?((int)(pagesize.getHeight() - h) ):0) + offsetY;
            Rectangle location = new Rectangle(oX, oY, oX + w, oY + h );

            PdfAnnotation stampAnnot = PdfAnnotation.createStamp(stamper.getWriter(), location, null, "ITEXT");
            stampAnnot.setFlags(PdfAnnotation.FLAGS_LOCKED);
            stampAnnot.setFlags(PdfAnnotation.FLAGS_READONLY);
            img.setAbsolutePosition(0, 0);
            PdfContentByte cb = new PdfContentByte(stamper.getWriter());
            PdfAppearance app = cb.createAppearance(w, h);
            app.addImage(img);
            stampAnnot.setAppearance(PdfName.N, app);
            stampAnnot.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_READONLY );

            stamper.addAnnotation(stampAnnot, stampPage);

            reader.close();
            stamper.close();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                document.close();
                stamp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public BufferedImage doPreview(byte[] pdfData, Integer page) {
        PDDocument pdfDocument = null;
        try {
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfData));
            Integer pagesCount = reader.getNumberOfPages();
            reader.close();
            Integer stampPage = pagesCount;
            if(page!=null){
                if(page>0&&page<pagesCount){
                    stampPage = page;
                }
                if(page>-pagesCount&&page<0){
                    stampPage = pagesCount + page + 1;
                }
            }
            pdfDocument = PDDocument.load(pdfData);
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
            int dpi = 300;
            BufferedImage bImage = pdfRenderer.renderImageWithDPI(stampPage-1, dpi, ImageType.RGB);
            return bImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ByteArrayOutputStream pagePreview(ByteArrayOutputStream pdfData, int page) {
        if(true) return pdfData;
        try {
            InputStream pdfStream = new ByteArrayInputStream(pdfData.toByteArray());
            PdfReader reader = new PdfReader(pdfStream);
            Integer pagesCount = reader.getNumberOfPages();
            Integer stampPage = pagesCount;
            if(page>0&&page<pagesCount){
                 stampPage = page;
             }
             if(page>-pagesCount&&page<0){
                 stampPage = pagesCount + page + 1;
             }

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
            PdfImportedPage thumbPage = writer.getImportedPage(reader, stampPage);
            Image image = Image.getInstance(thumbPage);
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(image.getOriginalData()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    Box processItems(StampData[] data) {
        Deque<Box> sizes = new ArrayDeque<>();

        int currentLineWidth = defaultLineWidth;
        int currentPadding = defaultPadding;
        int currentMargin = defaultMargin;
        String currentFamily = defaultFamily;
        String currentStyle = defaultStyle;
        int currentSize = defaultSize;

        // first pass - get max width and height
        for(StampData line:data) {
            int lineWidth = currentLineWidth; if(line.getWidth()!=null){lineWidth = line.getWidth();}
            int padding = currentPadding;if(line.getPadding()!=null){padding = line.getPadding();}
            int margin = currentMargin;if(line.getMargin()!=null){margin = line.getMargin();}
            if(line.getType()==null){
                if(line.getWidth()!=null){currentLineWidth = line.getWidth();}
                if(line.getMargin()!=null){ currentMargin=line.getMargin(); }
                if(line.getPadding()!=null){ currentPadding=line.getPadding(); }

                if(line.getFamily()!=null){ currentFamily=line.getFamily(); }
                if(line.getStyle()!=null){ currentStyle=line.getStyle(); }
                if(line.getSize()!=null){ currentSize=line.getSize(); }
            }

            if("rect".equals(line.getType()) || "roundrect".equals(line.getType())) {
                if(line.getItems()!=null) {
                    Box itemsBox = processItems(line.getItems());
                    itemsBox.setWidth(itemsBox.getWidth() + 2*(margin + padding+lineWidth));
                    itemsBox.setHeight(itemsBox.getHeight() + 2*(margin+padding+lineWidth));
                    sizes.push(itemsBox);
                }
            }
            if("line".equals(line.getType())) {
                sizes.push(new Box(0,2 * margin + lineWidth));
            }
            if("text".equals(line.getType())){
                String family = currentFamily;if(line.getFamily()!=null){family = line.getFamily();}
                String style = currentStyle;if(line.getStyle()!=null){style = line.getStyle();}
                int size = currentSize; if(line.getSize()!=null){ size = line.getSize();}

                Font font = new Font(family, "bold".equals(style)?Font.BOLD:("italic".equals(style)?Font.ITALIC:Font.PLAIN), size);

                Graphics2D tmpG = tmp.createGraphics(); tmpG.setFont(font); FontMetrics tmpFM = tmpG.getFontMetrics();

                sizes.push(new Box(tmpFM.stringWidth(line.getText()) + 2 * margin,tmpFM.getAscent() + 2 * margin));
            }
        }
        Box total = new Box(0,0);
        while (!sizes.isEmpty()){
            Box c = sizes.pop();
            total.setWidth(Math.max(total.getWidth(), c.getWidth()));
            total.setHeight(total.getHeight()+c.getHeight());
        }

        return total;
    }
    Box drawItems(StampData[] data, Graphics2D ig2) {
        return drawItems(data, ig2, new Box(0,0));
    }
    Box drawItems(StampData[] data, Graphics2D ig2, Box offset) {
        int currentHeight = 0;
        Deque<Box> sizes = new ArrayDeque<>();

        Box itemsBox = processItems(data);

        int currentLineWidth = defaultLineWidth;
        int currentPadding = defaultPadding;
        int currentMargin = defaultMargin;
        int currentRound = defaultRound;
        Color currentColor = defaultColor;
        String currentFamily = defaultFamily;
        String currentStyle = defaultStyle;
        int currentSize = defaultSize;

        for(StampData line:data) {
            int lineWidth = currentLineWidth; if(line.getWidth()!=null){lineWidth = line.getWidth();} ig2.setStroke(new BasicStroke(lineWidth));
            int padding = currentPadding;if(line.getPadding()!=null){padding = line.getPadding();}
            int margin = currentMargin;if(line.getMargin()!=null){margin = line.getMargin();}
            int round = currentRound; if(line.getRound()!=null){round = line.getRound();}

            Color color = currentColor; if(line.getColor()!=null&&line.getColor().startsWith("#")){color= StampService.HexToColor(line.getColor());} ig2.setPaint(color);

            if(line.getType()==null){
                if(line.getWidth()!=null){ currentLineWidth=line.getWidth(); ig2.setStroke(new BasicStroke(lineWidth)); }
                if(line.getMargin()!=null){ currentMargin=line.getMargin(); }
                if(line.getPadding()!=null){ currentPadding=line.getPadding(); }
                if(line.getRound()!=null){ currentRound=line.getRound(); }
                if(line.getColor()!=null){ currentColor= StampService.HexToColor(line.getColor()); ig2.setPaint(color); }

                if(line.getFamily()!=null){ currentFamily=line.getFamily(); }
                if(line.getStyle()!=null){ currentStyle=line.getStyle(); }
                if(line.getSize()!=null){ currentSize=line.getSize(); }

                if(line.getFamily()!=null || line.getStyle()!=null || line.getSize()!=null) {
                    Font font = new Font(currentFamily, "bold".equals(currentStyle)?Font.BOLD:("italic".equals(currentStyle)?Font.ITALIC:Font.PLAIN), currentSize);
                    ig2.setFont(font);
                }
            }

            if("rect".equals(line.getType()) || "roundrect".equals(line.getType())) {
                if(line.getItems()!=null) {
                    Box rectItemsBox = processItems(line.getItems());

                    if("rect".equals(line.getType())){
                        ig2.drawRect(0+margin+lineWidth/2+offset.getWidth(), currentHeight+margin+lineWidth+offset.getHeight(), rectItemsBox.getWidth()+padding*2+lineWidth, rectItemsBox.getHeight()+padding*2+lineWidth);
                    } else {
                        ig2.drawRoundRect(0+margin+lineWidth/2+offset.getWidth(), currentHeight+margin+lineWidth+offset.getHeight(), rectItemsBox.getWidth()+padding*2+lineWidth, rectItemsBox.getHeight()+padding*2+lineWidth, round, round);
                    }

                    Box offset1 = new Box(margin+lineWidth+padding+offset.getWidth(), currentHeight + margin+lineWidth+padding+offset.getHeight());
                    drawItems(line.getItems(), ig2, offset1);

                    currentHeight += rectItemsBox.getHeight()+2*(margin+padding+lineWidth);
                    sizes.push(new Box(rectItemsBox.getWidth()+2*(margin+padding+lineWidth),rectItemsBox.getHeight()-2*(margin+padding+lineWidth)));
                }
            }
            if("line".equals(line.getType())) {
                ig2.drawLine(0+offset.getWidth(), currentHeight+margin+lineWidth+offset.getHeight(), itemsBox.getWidth()+offset.getWidth(), currentHeight+margin+lineWidth+offset.getHeight());
                currentHeight += 2*margin + lineWidth;
                sizes.push(new Box(0,2*margin + lineWidth));
            }
            if("text".equals(line.getType())){
                String family = currentFamily;if(line.getFamily()!=null){family = line.getFamily();}
                String style = currentStyle;if(line.getStyle()!=null){style = line.getStyle();}
                int size = currentSize; if(line.getSize()!=null){ size = line.getSize();}

                Font font = new Font(family, "bold".equals(style)?Font.BOLD:("italic".equals(style)?Font.ITALIC:Font.PLAIN), size);
                ig2.setFont(font);
                FontMetrics fontMetrics = ig2.getFontMetrics();

                String message = line.getText();
                int textWidth = fontMetrics.stringWidth(message);
                int xPos = (itemsBox.getWidth() - textWidth) / 2;
                if("left".equals(line.getPosition())) {
                    xPos = 0 + margin;
                }
                if("right".equals(line.getPosition())){
                    xPos = itemsBox.getWidth() - textWidth - margin;
                }
                int yPos = fontMetrics.getAscent() + margin;
                ig2.drawString(line.getText(), xPos+offset.getWidth(), currentHeight + yPos+offset.getHeight());
                currentHeight += yPos + margin;
                sizes.push(new Box(fontMetrics.stringWidth(line.getText())+2*margin,fontMetrics.getAscent()+2*margin));
            }
        }
        Box itemsTotal = new Box(0,0);
        while (!sizes.isEmpty()){
            Box c = sizes.pop();
            itemsTotal.setWidth(Math.max(itemsTotal.getWidth(), c.getWidth()));
            itemsTotal.setHeight(itemsTotal.getHeight()+c.getHeight());
        }
        return itemsTotal;
    }

    @Override
    public BufferedImage makeStampImage(String json) throws JsonProcessingException {
        StampData[] data = new ObjectMapper().readValue(json, StampData[].class);
        Box total = processItems(data);
        BufferedImage bi = new BufferedImage(total.getWidth(), total.getHeight()+50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();
        drawItems(data, ig2);
        return bi;
    }


}
