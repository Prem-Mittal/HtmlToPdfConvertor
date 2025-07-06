package com.example.pdfcontent.Manager.Impl;

import com.example.pdfcontent.Manager.PdfGeneration;
import com.example.pdfcontent.Service.Dto.RequestDto.PdfCreationDto;
import com.example.pdfcontent.Service.PdfContentProcessorImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class PdfGenerationImpl implements PdfGeneration {

    private final PdfContentProcessorImpl pdfContentProcessor;

    @Override
    public String generatePdf(MultipartFile frontendcss, MultipartFile additional, MultipartFile headerfootercss, PdfCreationDto pdfCreationDto) {
        return pdfContentProcessor.processPdfContent(frontendcss, additional, headerfootercss, pdfCreationDto);
    }
}
