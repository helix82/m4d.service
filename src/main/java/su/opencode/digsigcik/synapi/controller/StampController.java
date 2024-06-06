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


    private byte[] doStamp(InputStream document,InputStream stamp) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfReader reader = new PdfReader(document);
            PdfStamper stamper = new PdfStamper(reader, baos, '\0', true);

            //Read the image
            Image img = Image.getInstance(ImageIO.read(stamp), null);

            float w = img.getScaledWidth();
            float h = img.getScaledHeight();
            Rectangle location = new Rectangle(70, 770 - h, 70 + w, 770);

            PdfAnnotation stampAnnot = PdfAnnotation.createStamp(stamper.getWriter(), location, null, "ITEXT");
            img.setAbsolutePosition(0, 0);
            PdfContentByte cb = new PdfContentByte(stamper.getWriter());
            PdfAppearance app = cb.createAppearance(w, h);
            app.addImage(img);
            stampAnnot.setAppearance(PdfName.N, app);
            stampAnnot.setFlags(PdfAnnotation.FLAGS_PRINT);

            stamper.addAnnotation(stampAnnot, 1);
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

    @GetMapping("/")
    public @ResponseBody
    byte[] stamp(HttpServletResponse httpResponse,
                 @RequestParam("document") String documentUrl,
                 @RequestParam("stamp") String stampUrl) {
        try {
            httpResponse.setContentType ("application/pdf");
            return doStamp(new URL(documentUrl).openStream(), new URL(stampUrl).openStream());
        } catch (IOException e) {
            return null;
        }
    }

    @PostMapping("/")
    public @ResponseBody
    byte[] stamp(
            HttpServletResponse httpResponse,
            @RequestPart("document") MultipartFile document,
            @RequestPart("stamp") MultipartFile stamp) {
        try {
            httpResponse.setContentType ("application/pdf");
            return doStamp(document.getInputStream(), stamp.getInputStream());
        } catch (IOException e) {
            return null;
        }
    }
  }
