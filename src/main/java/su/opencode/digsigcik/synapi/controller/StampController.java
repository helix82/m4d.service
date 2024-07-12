package su.opencode.digsigcik.synapi.controller;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.service.StampService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

@RestController
@RequestMapping("/api/v1/stamp")
@AllArgsConstructor
public class StampController {

    @Autowired
    StampService stampService;

    @Operation(summary = "Проставить штамп на визуальном представлении документа",
            description = "Возвращает документ PDF с проставленным штампом" )
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

        return stampService.doStamp(documentUrl, stampUrl, page, offsetX, offsetY, scale);
    }

    @Operation(summary = "Проставить штамп на визуальном представлении документа, превью",
            description = "Возвращает превью документа PDF с проставленным штампом" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Штамп успешно проставлен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping(value = "/preview", produces = "image/png")
    public @ResponseBody
    byte[] preview(@RequestParam("document") @Parameter(name = "document", description="Ссылка на docx или pdf документ", example = "http://example.com/document.pdf", required = true) String documentUrl,
                 @RequestParam("stamp") @Parameter(name = "stamp", description="Ссылка на файл штампа", example = "http://example.com/stamp.png", required = true) String stampUrl,
                 @RequestParam(value = "page", required = false) @Parameter(name = "page", description="Номер страницы для проставления штампа. Может быть отрицательным, тогда номер страницы считается с конца документа", example = "-1") Integer page,
                 @RequestParam(value = "offsetX", required = false) @Parameter(name = "offsetX", description="Смещение штампа в 1/72 дюйма оносительно левой стороны страницы. Может быть отрицательным, тогда смещение считается от правой стороны страницы", example = "36") Integer offsetX,
                 @RequestParam(value = "offsetY", required = false) @Parameter(name = "offsetY", description="Смещение штампа в 1/72 дюйма оносительно нижней стороны страницы. Может быть отрицательным, тогда смещение считается от верхней стороны страницы", example = "36") Integer offsetY,
                 @RequestParam(value = "scale", required = false) @Parameter(name = "scale", description="Масштаб штампа в процентах, относительно исходного размера", example = "75") Integer scale) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(stampService.doPreview(stampService.doStamp(documentUrl, stampUrl, page, offsetX, offsetY, scale), page), "png", baos);
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(baos!=null)baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Operation(summary = "Проставить штамп на визуальном представлении документа",
            description = "Возвращает документ PDF с проставленным штампом" )
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
        InputStream stampStream = null;
        try {
            if(stampdata!=null && !stampdata.isEmpty()){
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(stampService.makeStampImage(stampdata), "png", os);
                stampStream = new ByteArrayInputStream(os.toByteArray());
            } else {
                stampStream = stamp.getInputStream();
            }
            if(document.getOriginalFilename().endsWith(".docx")) {
                return stampService.doStampDocx(document.getInputStream(), stampStream, page, offsetX, offsetY, scale);
            } else {
                return stampService.doStampPdf(document.getInputStream(), stampStream, page, offsetX, offsetY, scale);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(stampStream!=null)stampStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    @Operation(summary = "Проставить штамп на визуальном представлении документа, превью",
            description = "Возвращает превью документа PDF с проставленным штампом" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Штамп успешно проставлен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/preview", produces = "image/png")
    public @ResponseBody
    byte[] preview(@RequestPart("document") @Parameter(name = "document", description="Содержимое PDF-документа", example = "http://example.com/document.pdf", required = true) MultipartFile document,
                   @RequestPart(value = "stamp", required = false) @Parameter(name = "stamp", description="Содержимое файла штампа", example = "http://example.com/stamp.png") MultipartFile stamp,
                   @RequestParam(value = "stampdata", required = false) @Parameter(name = "stampdata", description="Описание штампа", example = "[{\"text\":\"Подписано: Иванов Иван Иванович\"},{\"text\":\"02.02.2024\",\"position\":\"right\"}]") String stampdata,
                   @RequestParam(value = "page", required = false) @Parameter(name = "page", description="Номер страницы для проставления штампа. Может быть отрицательным, тогда номер страницы считается с конца документа", example = "-1") Integer page,
                   @RequestParam(value = "offsetX", required = false) @Parameter(name = "offsetX", description="Смещение штампа в 1/72 дюйма оносительно левой стороны страницы. Может быть отрицательным, тогда смещение считается от правой стороны страницы", example = "36") Integer offsetX,
                   @RequestParam(value = "offsetY", required = false) @Parameter(name = "offsetY", description="Смещение штампа в 1/72 дюйма оносительно нижней стороны страницы. Может быть отрицательным, тогда смещение считается от верхней стороны страницы", example = "36") Integer offsetY,
                   @RequestParam(value = "scale", required = false) @Parameter(name = "scale", description="Масштаб штампа в процентах, относительно исходного размера", example = "75") Integer scale) {
        InputStream stampStream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if(stampdata!=null && !stampdata.isEmpty()){
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(stampService.makeStampImage(stampdata), "png", os);
                stampStream = new ByteArrayInputStream(os.toByteArray());
            } else {
                stampStream = stamp.getInputStream();
            }
            if(document.getOriginalFilename().endsWith(".docx")) {
                ImageIO.write(stampService.doPreview(stampService.doStampDocx(document.getInputStream(), stampStream, page, offsetX, offsetY, scale), page), "png", baos);
                baos.flush();
                return baos.toByteArray();
            } else {
                ImageIO.write(stampService.doPreview(stampService.doStampPdf(document.getInputStream(), stampStream, page, offsetX, offsetY, scale), page), "png", baos);
                baos.flush();
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(stampStream!=null)stampStream.close();
                if(baos!=null)baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
            ImageIO.write(stampService.makeStampImage(json), "png", baos);
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
