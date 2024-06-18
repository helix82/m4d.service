package su.opencode.digsigcik.synapi.controller;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@RestController
@RequestMapping("/api/v1/stamp")
@AllArgsConstructor
public class StampController {

    private byte[] doStamp(InputStream document,InputStream stamp, Integer page, Integer offsetX, Integer offsetY, Integer scale) {
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
    byte[] stamp(@RequestParam("document") @Parameter(name = "document", description="Ссылка на PDF-документ", example = "http://example.com/document.pdf", required = true) String documentUrl,
                 @RequestParam("stamp") @Parameter(name = "stamp", description="Ссылка на файл штампа", example = "http://example.com/stamp.png", required = true) String stampUrl,
                 @RequestParam(value = "page", required = false) @Parameter(name = "page", description="Номер страницы для проставления штампа. Может быть отрицательным, тогда номер страницы считается с конца документа", example = "-1") Integer page,
                 @RequestParam(value = "offsetX", required = false) @Parameter(name = "offsetX", description="Смещение штампа в 1/72 дюйма оносительно левой стороны страницы. Может быть отрицательным, тогда смещение считается от правой стороны страницы", example = "36") Integer offsetX,
                 @RequestParam(value = "offsetY", required = false) @Parameter(name = "offsetY", description="Смещение штампа в 1/72 дюйма оносительно нижней стороны страницы. Может быть отрицательным, тогда смещение считается от верхней стороны страницы", example = "36") Integer offsetY,
                 @RequestParam(value = "scale", required = false) @Parameter(name = "scale", description="Масштаб штампа в процентах, относительно исходного размера", example = "75") Integer scale) {
        try {
            return doStamp(new URL(documentUrl).openStream(), new URL(stampUrl).openStream(), page, offsetX, offsetY, scale);
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
                 @RequestPart("stamp") @Parameter(name = "stamp", description="Содержимое файлв штампа", example = "http://example.com/stamp.png", required = true) MultipartFile stamp,
                 @RequestPart(value = "page", required = false) @Parameter(name = "page", description="Номер страницы для проставления штампа. Может быть отрицательным, тогда номер страницы считается с конца документа", example = "-1") Integer page,
                 @RequestPart(value = "offsetX", required = false) @Parameter(name = "offsetX", description="Смещение штампа в 1/72 дюйма оносительно левой стороны страницы. Может быть отрицательным, тогда смещение считается от правой стороны страницы", example = "36") Integer offsetX,
                 @RequestPart(value = "offsetY", required = false) @Parameter(name = "offsetY", description="Смещение штампа в 1/72 дюйма оносительно нижней стороны страницы. Может быть отрицательным, тогда смещение считается от верхней стороны страницы", example = "36") Integer offsetY,
                 @RequestParam(value = "scale", required = false) @Parameter(name = "scale", description="Масштаб штампа в процентах, относительно исходного размера", example = "75") Integer scale) {
        try {
            return doStamp(document.getInputStream(), stamp.getInputStream(), page, offsetX, offsetY, scale);
        } catch (IOException e) {
            return null;
        }
    }
  }
