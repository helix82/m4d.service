package su.opencode.digsigcik.synapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.StampData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

@RestController
@RequestMapping("/api/v1/stamp")
@AllArgsConstructor
public class StampController {

    private byte[] doStamp(InputStream document, InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

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
        }
        return null;
    }

    @Operation(summary = "Проставить штамп на визуальном представлении документа",
            description = "Возвращает документ PDF с просталенным штампом" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Штамп успешно проставлен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping(value = "/", produces = "application/pdf")
    public @ResponseBody
    byte[] stamp(@RequestParam("document") @Parameter(name = "document", description="Ссылка на docx или pdf документ", example = "http://example.com/document.pdf", required = true) String documentUrl,
                 @RequestParam("stamp") @Parameter(name = "stamp", description="Ссылка на файл штампа", example = "http://example.com/stamp.png", required = true) String stampUrl,
                 @RequestParam(value = "page", required = false) @Parameter(name = "page", description="Номер страницы для проставления штампа. Может быть отрицательным, тогда номер страницы считается с конца документа", example = "-1") Integer page,
                 @RequestParam(value = "offsetX", required = false) @Parameter(name = "offsetX", description="Смещение штампа в 1/72 дюйма оносительно левой стороны страницы. Может быть отрицательным, тогда смещение считается от правой стороны страницы", example = "36") Integer offsetX,
                 @RequestParam(value = "offsetY", required = false) @Parameter(name = "offsetY", description="Смещение штампа в 1/72 дюйма оносительно нижней стороны страницы. Может быть отрицательным, тогда смещение считается от верхней стороны страницы", example = "36") Integer offsetY,
                 @RequestParam(value = "scale", required = false) @Parameter(name = "scale", description="Масштаб штампа в процентах, относительно исходного размера", example = "75") Integer scale) {
        try {
            InputStream pdfStream=null;
            if(documentUrl.endsWith(".docx")){
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PdfOptions options = PdfOptions.create();
                PdfConverter.getInstance().convert(new XWPFDocument(new URL(documentUrl).openStream()), outputStream, options);
                pdfStream = new ByteArrayInputStream(outputStream.toByteArray());
            } else {
                pdfStream = new URL(documentUrl).openStream();
            }
            return doStamp(pdfStream, new URL(stampUrl).openStream(), page, offsetX, offsetY, scale);

        } catch (IOException e) {
            return null;
        }
    }


    @Operation(summary = "Проставить штамп на визуальном представлении документа",
            description = "Возвращает документ PDF с просталенным штампом" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Штамп успешно проставлен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/", produces = "application/pdf")
    public @ResponseBody
    byte[] stamp(@RequestPart("document") @Parameter(name = "document", description="Содержимое PDF-документа", example = "http://example.com/document.pdf", required = true) MultipartFile document,
                 @RequestPart(value = "stamp", required = false) @Parameter(name = "stamp", description="Содержимое файла штампа", example = "http://example.com/stamp.png") MultipartFile stamp,
                 @RequestParam(value = "stampdata", required = false) @Parameter(name = "stampdata", description="Описание штампа", example = "[{\"text\":\"Подписано: Иванов Иван Иванович\"},{\"text\":\"02.02.2024\",\"position\":\"right\"}]") String stampdata,
                 @RequestParam(value = "page", required = false) @Parameter(name = "page", description="Номер страницы для проставления штампа. Может быть отрицательным, тогда номер страницы считается с конца документа", example = "-1") Integer page,
                 @RequestParam(value = "offsetX", required = false) @Parameter(name = "offsetX", description="Смещение штампа в 1/72 дюйма оносительно левой стороны страницы. Может быть отрицательным, тогда смещение считается от правой стороны страницы", example = "36") Integer offsetX,
                 @RequestParam(value = "offsetY", required = false) @Parameter(name = "offsetY", description="Смещение штампа в 1/72 дюйма оносительно нижней стороны страницы. Может быть отрицательным, тогда смещение считается от верхней стороны страницы", example = "36") Integer offsetY,
                 @RequestParam(value = "scale", required = false) @Parameter(name = "scale", description="Масштаб штампа в процентах, относительно исходного размера", example = "75") Integer scale) {
        try {
            InputStream pdfStream=null;

            if(document.getOriginalFilename().endsWith(".docx")){
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PdfOptions options = PdfOptions.create();
                PdfConverter.getInstance().convert(new XWPFDocument(document.getInputStream()), outputStream, options);
                pdfStream = new ByteArrayInputStream(outputStream.toByteArray());
            } else {
                pdfStream = document.getInputStream();
            }
            if(stampdata!=null && !stampdata.isEmpty()){
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(makeStampImage(stampdata), "png", os);
                return doStamp(pdfStream, new ByteArrayInputStream(os.toByteArray()), page, offsetX, offsetY, scale);

            } else {
                return doStamp(pdfStream, stamp.getInputStream(), page, offsetX, offsetY, scale);
            }
        } catch (IOException e) {
            return null;
        }
    }
    private class Box {
        int width, height;

        public Box(int w, int h) {
            this.width = w;
            this.height = h;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
    private final static int defaultLineWidth = 3;
    private final static int defaultPadding = 0;
    private final static int defaultMargin = 0;
    private final static int defaultRound = 10;
    private final static int defaultWidth = 0;
    private final static int defaultHeight = 0;
    private final static Color defaultColor = new Color(0,0,0,255);
    private final static String defaultFamily = "TimesRoman";
    private final static String defaultStyle = "bold"; //Font.BOLD
    private final static int defaultSize= 14;
    private final static BufferedImage tmp = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

    public static Color HexToColor(String hex) {
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

    Box processItems(StampData[] data) {
        Deque<Box> sizes = new ArrayDeque<Box>();

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
        Deque<Box> sizes = new ArrayDeque<Box>();

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
            Color color = currentColor; if(line.getColor()!=null&&line.getColor().startsWith("#")){color=HexToColor(line.getColor());} ig2.setPaint(color);

            if(line.getType()==null){
                if(line.getWidth()!=null){ currentLineWidth=line.getWidth(); ig2.setStroke(new BasicStroke(lineWidth)); }
                if(line.getMargin()!=null){ currentMargin=line.getMargin(); }
                if(line.getPadding()!=null){ currentPadding=line.getPadding(); }
                if(line.getRound()!=null){ currentRound=line.getRound(); }
                if(line.getColor()!=null){ currentColor=HexToColor(line.getColor()); ig2.setPaint(color); }

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
    BufferedImage makeStampImage(String json) throws JsonProcessingException {
        StampData[] data = new ObjectMapper().readValue(json, StampData[].class);
        Box total = processItems(data);
        BufferedImage bi = new BufferedImage(total.getWidth(), total.getHeight()+50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();
        drawItems(data, ig2);
        return bi;
    }

    @Operation(summary = "Создать штамп по описанию",
            description = "Создаёт штамп по описанию" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Штамп успешно создан" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/make", produces = "image/png")
    public @ResponseBody
    byte[] makeStamp(@RequestBody String json) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(makeStampImage(json), "png", baos);
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
  }
