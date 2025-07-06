package com.example.pdfcontent.Manager;

import com.example.pdfcontent.Service.Dto.RequestDto.PdfCreationDto;
import org.springframework.web.multipart.MultipartFile;

public interface PdfGeneration {

    String generatePdf(MultipartFile header, MultipartFile footer, MultipartFile body, PdfCreationDto pdfCreationDto);
}
