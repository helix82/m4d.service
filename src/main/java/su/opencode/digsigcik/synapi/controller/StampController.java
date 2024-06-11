package su.opencode.digsigcik.synapi.controller;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ByteBuffer;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfTemplate;
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
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

@RestController
@RequestMapping("/api/v1/stamp")
@AllArgsConstructor
public class StampController {

    private static void setVisibleSignatureRotated(PdfStamper stamper, PdfSignatureAppearance appearance, Rectangle pageRect, int page, String fieldName) throws DocumentException, IOException {
        float height = pageRect.getHeight();
        float width = pageRect.getWidth();
        float llx = pageRect.getLeft();
        float lly = pageRect.getBottom();
        // Visual signature is configured as if it were going to be a regular horizontal visual signature.
        appearance.setVisibleSignature(new Rectangle(llx, lly, llx + height, lly + width), page, null);
        // We trigger premature appearance creation, so independent parts of it can be modified right away.
        appearance.getAppearance();
        // Now we correct the width and height.
        appearance.setVisibleSignature(new Rectangle(llx, lly, llx + width, lly + height), page, fieldName);
        appearance.getTopLayer().setWidth(width);
        appearance.getTopLayer().setHeight(height);
        PdfTemplate n2Layer = appearance.getLayer(2);
        n2Layer.setWidth(width);
        n2Layer.setHeight(height);
        // Then we rotate the n2 layer. See http://developers.itextpdf.com/question/how-rotate-paragraph.
        PdfTemplate t = PdfTemplate.createTemplate(stamper.getWriter(), height, width);
        ByteBuffer internalBuffer = t.getInternalBuffer();
        internalBuffer.write(n2Layer.toString().getBytes());
        n2Layer.reset();
        Image textImg = Image.getInstance(t);
        textImg.setInterpolation(true);
        textImg.scaleAbsolute(height, width);
        textImg.setRotationDegrees((float) 90);
        textImg.setAbsolutePosition(0, 0);
        n2Layer.addImage(textImg);
    }

    private byte[] doStamp(InputStream document,InputStream stamp) {
        try {
/*            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream("signer.p12"), "secret".toCharArray());
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, "secret".toCharArray());
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);


*/
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfReader reader = new PdfReader(document);
            PdfStamper stamper = new PdfStamper(reader, baos, '\0', true);
//            PdfStamper stamper  = PdfStamper.createSignature(reader, baos, '\0');

//            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//            appearance.setCertificate(certificateChain[0]);

//            setVisibleSignatureRotated(stamper, appearance, new Rectangle(120, 650, 170, 770), 1, null);



            //Read the image
            Image img = Image.getInstance(ImageIO.read(stamp), null);

            float w = img.getScaledWidth();
            float h = img.getScaledHeight();

            Rectangle location = new Rectangle(70, 70 - h, 70 + w, 70);

            PdfAnnotation stampAnnot = PdfAnnotation.createStamp(stamper.getWriter(), location, null, "ITEXT");
            stampAnnot.setFlags(PdfAnnotation.FLAGS_LOCKED);
            stampAnnot.setFlags(PdfAnnotation.FLAGS_READONLY);
            img.setAbsolutePosition(0, 0);
            PdfContentByte cb = new PdfContentByte(stamper.getWriter());
            PdfAppearance app = cb.createAppearance(w, h);
            app.addImage(img);
            stampAnnot.setAppearance(PdfName.N, app);
            stampAnnot.setFlags(PdfAnnotation.FLAGS_PRINT);

            stamper.addAnnotation(stampAnnot, reader.getNumberOfPages());

/*            if (stampAnnot!=null)
                sap.SetVisibleSignature(stampAnnot, 1, "SignatureESift");
*/
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
