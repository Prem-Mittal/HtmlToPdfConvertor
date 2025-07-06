package com.example.pdfcontent.Service;

import com.example.pdfcontent.Service.Dto.RequestDto.PdfCreationDto;
import org.springframework.web.multipart.MultipartFile;

public interface PdfContentProcessor {

    String processPdfContent(MultipartFile frontendcss, MultipartFile additional, MultipartFile headerfootercss, PdfCreationDto pdfCreationDto);
}
