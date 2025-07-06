package com.example.pdfcontent.Controller;

import com.example.pdfcontent.Manager.Impl.PdfGenerationImpl;
import com.example.pdfcontent.Service.Dto.RequestDto.PdfCreationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/communications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class IntakePdf {



    private final PdfGenerationImpl pdfGenerationImpl;

    @PostMapping("/download_base_url")
    public ResponseEntity<String> downloadbaseUrl(@RequestParam("headerfootercssFile") MultipartFile headerfootercss,
                                                  @RequestParam("frontendadditionalcssfile") MultipartFile additional,
                                                  @RequestParam("frontendcssfile") MultipartFile frontendcss,
                                                  @RequestParam("pdfCreationDto") String pdfCreationDtoo) {
        PdfCreationDto pdfCreationDto = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            pdfCreationDto = objectMapper.readValue(pdfCreationDtoo, PdfCreationDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body(pdfGenerationImpl.generatePdf(frontendcss, additional, headerfootercss, pdfCreationDto));
    }

}
