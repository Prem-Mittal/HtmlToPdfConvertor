package com.example.pdfcontent.Service.Dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PdfCreationDto {

    @JsonProperty("headerContent")
    private String headerContent;

    @JsonProperty("footerContent")
    private String footerContent;

    @JsonProperty("bodyContent")
    private String bodyContent;
}
