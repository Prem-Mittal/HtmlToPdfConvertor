package com.example.pdfcontent.Service;

import com.example.pdfcontent.Service.Dto.RequestDto.PdfCreationDto;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.font.FontProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class PdfContentProcessorImpl implements PdfContentProcessor{

    private final AddCssfiles addCssfiles;

    @Override
    public String processPdfContent(MultipartFile frontendcss, MultipartFile additional, MultipartFile headerfootercss, PdfCreationDto pdfCreationDto) {
        try {
            MultipartFile[] files={frontendcss, additional, headerfootercss};
            return ProcessPdfContent(pdfCreationDto,files);
        } catch (Exception e) {
            throw new RuntimeException("error occured check logs");
        }
    }

    private String ProcessPdfContent(PdfCreationDto pdfCreationDto, MultipartFile[] files)throws Exception{
        String htmlContent = pdfCreationDto.getBodyContent();
        String headerContent = pdfCreationDto.getHeaderContent();
        String footerContent= pdfCreationDto.getFooterContent();
        if (htmlContent == null || htmlContent.isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be null or empty");
        }

        log.info("Starting PDF content processing");
        boolean hasHeaderFooter = (headerContent != null && !headerContent.trim().isEmpty()) ||
                (footerContent != null && !footerContent.trim().isEmpty());
        String processedContent = addCssfiles.processHtmlForPdf(htmlContent, false, hasHeaderFooter, files);
        if(headerContent!=null && !headerContent.trim().isEmpty()) {
            headerContent = addCssfiles.processContentForPdf(headerContent,files);
        }
        if(footerContent!=null && !footerContent.trim().isEmpty()) {
            footerContent = addCssfiles.processContentForPdf(footerContent,files);
        }

        byte[] pdfData = createPdfBytesWithHeaderFooter(processedContent, headerContent, footerContent);

        String base64Encoded = Base64.getEncoder().encodeToString(pdfData);
        return base64Encoded;
    }

    private byte[] createPdfBytesWithHeaderFooter(String processedContent, String headerContent, String footerContent) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        pdfDocument.setDefaultPageSize(PageSize.LETTER);

        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setCreateAcroForm(false);

        FontProvider fontProvider = new FontProvider();
        fontProvider.addStandardPdfFonts();
        addFontFromResources(fontProvider, "/fonts/TevaSans-Regular.otf");
        addFontFromResources(fontProvider, "/fonts/TevaSans-BoldItalic.otf");
        addFontFromResources(fontProvider, "/fonts/TevaSans-Italic.otf");
        addFontFromResources(fontProvider, "/fonts/TevaSans-Light.otf");
        addFontFromResources(fontProvider, "/fonts/TevaSans-LightItalic.otf");
        addFontFromResources(fontProvider, "/fonts/TevaSans-Bold.otf");
        addFontFromResources(fontProvider, "/fonts/Calibri Bold Italic.otf");
        addFontFromResources(fontProvider, "/fonts/Calibri Bold.otf");
        addFontFromResources(fontProvider, "/fonts/Calibri Italic.otf");
        addFontFromResources(fontProvider, "/fonts/Calibri Light.otf");
        addFontFromResources(fontProvider, "/fonts/Calibri Light Italic.otf");
        addFontFromResources(fontProvider, "/fonts/Calibri Regular.otf");
        converterProperties.setFontProvider(fontProvider);

        boolean hasHeader = headerContent != null && !headerContent.trim().isEmpty();
        boolean hasFooter = footerContent != null && !footerContent.trim().isEmpty();
        Document document = new Document(pdfDocument, PageSize.LETTER);
        document.setMargins(0, 0, 0, 0);
        if (hasHeader || hasFooter) {
            float bottomMargin = 50f;
            float headerMargin = 100f;
            if (hasFooter) {
                float footerHeight = measureFooterHeight(footerContent, converterProperties);
                bottomMargin = bottomMargin+ footerHeight;
            }
            document.setMargins(headerMargin, 0, bottomMargin, 0);
            PdfHeaderFooterHandlerService handler = new PdfHeaderFooterHandlerService(
                    hasHeader ? headerContent : null,
                    hasFooter ? footerContent : null,
                    bottomMargin,
                    headerMargin,
                    converterProperties
            );

            if (hasHeader) {
                pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, handler);
            }
            if (hasFooter) {
                pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, handler);
            }
        }

        try (InputStream stream = new ByteArrayInputStream(processedContent.getBytes(StandardCharsets.UTF_8))) {
            List<IElement> elements = HtmlConverter.convertToElements(stream, converterProperties);
            for (IElement element : elements) {
                if (element instanceof IBlockElement) {
                    document.add((IBlockElement) element);
                }
            }
            document.close();
        }

        return byteArrayOutputStream.toByteArray();

    }

    public float measureFooterHeight(String footerHtml, ConverterProperties converterProperties)  {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        PdfDocument tempPdf = new PdfDocument(new PdfWriter(tempStream));
        tempPdf.setDefaultPageSize(PageSize.LETTER);
        Document tempDocument = new Document(tempPdf);
        float[] height = {0};

        List<IElement> elements = HtmlConverter.convertToElements(footerHtml, converterProperties);
        for (IElement element : elements) {
            if (element instanceof IBlockElement) {
                IBlockElement block = (IBlockElement) element;
                float previousHeight = tempDocument.getRenderer().getCurrentArea().getBBox().getHeight();
                tempDocument.add(block);
                float newHeight = tempDocument.getRenderer().getCurrentArea().getBBox().getHeight();
                height[0] += previousHeight - newHeight;
            }
        }
        tempDocument.close();
        tempPdf.close();
        return height[0];
    }



    private void addFontFromResources(FontProvider fontProvider, String resourcePath) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream(resourcePath)) {
            if (fontStream == null) {
                throw new FileNotFoundException("Font file not found: " + resourcePath);
            }
            String extension = resourcePath.toLowerCase().endsWith(".ttf") ? ".ttf" : ".otf";
            File tempFontFile = File.createTempFile("font", extension);
            tempFontFile.deleteOnExit();
            try (OutputStream os = new FileOutputStream(tempFontFile)) {
                fontStream.transferTo(os);
            }
            fontProvider.addFont(tempFontFile.getAbsolutePath());
        }
    }


}
