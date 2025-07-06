package com.example.pdfcontent.Service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@RequiredArgsConstructor


public class PdfHeaderFooterHandlerService implements IEventHandler {

    private String headerHtml;
    private String footerHtml;
    private float footerHeight;
    private float headerHeight;
    private ConverterProperties converterProperties;

    public PdfHeaderFooterHandlerService(String headerHtml, String footerHtml, Float footerHeight, Float headerHeight, ConverterProperties converterProperties) {
        this.headerHtml = headerHtml;
        this.footerHtml = footerHtml;
        this.footerHeight = footerHeight;
        this.headerHeight = headerHeight;
        this.converterProperties = converterProperties;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
        Rectangle pageSize = page.getPageSize();

        try {
            if (event.getType().equals(PdfDocumentEvent.START_PAGE)) {
                if (headerHtml != null && !headerHtml.trim().isEmpty()) {
                    Rectangle headerArea = new Rectangle(
                            pageSize.getLeft(),
                            pageSize.getTop() - headerHeight,
                            pageSize.getWidth(),
                            headerHeight
                    );
                    Canvas headerCanvas = new Canvas(canvas, headerArea);


                    converterProperties.setCreateAcroForm(false);
                    List<IElement> headerElements = HtmlConverter.convertToElements(headerHtml, converterProperties);
                    for (IElement element : headerElements) {
                        if (element instanceof IBlockElement) {
                            headerCanvas.add((IBlockElement) element);
                        }
                    }
                    headerCanvas.close();
                }
            }

            if (event.getType().equals(PdfDocumentEvent.END_PAGE)) {
                if (footerHtml != null && !footerHtml.trim().isEmpty()) {
                    Rectangle footerArea = new Rectangle(
                            pageSize.getLeft(),
                            pageSize.getBottom(),
                            pageSize.getWidth(),
                            footerHeight
                    );

                    Canvas footerCanvas = new Canvas(canvas, footerArea);


                    converterProperties.setCreateAcroForm(false);
                    List<IElement> footerElements = HtmlConverter.convertToElements(footerHtml, converterProperties);
                    for (IElement element : footerElements) {
                        if (element instanceof IBlockElement) {
                            footerCanvas.add((IBlockElement) element);
                        }
                    }
                    footerCanvas.close();
                }
            }

        } catch (Exception e) {
            log.error("Erro in handler");
        }
    }


}
