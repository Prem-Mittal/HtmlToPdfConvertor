package com.example.pdfcontent.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class AddCssfiles {
    private static final String LESS_THAN_OPERATOR_REGEX_CONST = "<<";
    private static final String LESS_THAN_OPERATOR_CONST = "&lt;&lt;";
    private static final String GREATER_THAN_OPERATOR_REGEX_CONST = ">>";
    private static final String GREATER_THAN_OPERATOR_CONST = "&gt;&gt;";
    private static final String HASH_OPERATOR_REGEX_CONST = "\\^#";
    private static final String HASH_OPERATOR_CONST = "#";


    public String processHtmlForPdf(String htmlContent, boolean isDocusign, boolean hasHeaderFooter, MultipartFile[] files) throws Exception {
        log.info("Adding css");
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }
        if (isDocusign) {
            htmlContent = removeDocusignDiv(htmlContent);
        }
        htmlContent = combineHtmlWithCss(htmlContent,files);
        htmlContent = convertHslCssToRgb(htmlContent);
        htmlContent = fixTextFlow(htmlContent);
        htmlContent = replaceSpecialOperators(htmlContent);
        return htmlContent;
    }

    public String fixTextFlow(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }
        Document document = Jsoup.parse(htmlContent);
        Elements allElements = document.select("*");
        for (Element element : allElements) {
            String tagName = element.tagName().toLowerCase();
            String currentStyle = element.attr("style");
            switch (tagName) {
                case "p", "div", "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    if (!currentStyle.contains("display")) {
                        element.attr("style", currentStyle + " display: block;");
                    }
                }
                case "span", "a", "strong", "em", "b", "i", "small", "label" -> {
                    if (!currentStyle.contains("display")) {
                        element.attr("style", currentStyle + " display: inline;");
                    }
                }
                case "img" -> {
                    // Ensure images don't break text flow
                    if (!currentStyle.contains("vertical-align")) {
                        element.attr("style", currentStyle + " vertical-align: top;");
                    }
                }
            }
            if (currentStyle.contains("writing-mode")) {
                String fixedStyle = currentStyle.replaceAll("writing-mode\\s*:\\s*[^;]+;?", "");
                element.attr("style", fixedStyle);
            }
            if (currentStyle.contains("transform") && currentStyle.contains("rotate")) {
                String fixedStyle = currentStyle.replaceAll("transform\\s*:\\s*[^;]+;?", "");
                element.attr("style", fixedStyle);
            }
        }

        return document.html();
    }

    public String processContentForPdf(String htmlContent, MultipartFile[] files) throws Exception {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }
        htmlContent = combineHtmlWithCssforHeaderFooter(htmlContent,files);
        htmlContent = convertHslCssToRgb(htmlContent);
        htmlContent = replaceSpecialOperators(htmlContent);

        return htmlContent;
    }

    public String replaceSpecialOperators(String htmlContent) {
        return htmlContent
                .replace(LESS_THAN_OPERATOR_REGEX_CONST, LESS_THAN_OPERATOR_CONST)
                .replace(GREATER_THAN_OPERATOR_REGEX_CONST, GREATER_THAN_OPERATOR_CONST)
                .replace("\\\"", "\"")
                .replaceAll(HASH_OPERATOR_REGEX_CONST, HASH_OPERATOR_CONST);
    }

    public String convertHslCssToRgb(String htmlContent) {
        Pattern pattern = Pattern.compile("hsl\\((\\d+),\\s*(\\d+)%?,\\s*(\\d+)%?\\)");
        Matcher matcher = pattern.matcher(htmlContent);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            int h = Integer.parseInt(matcher.group(1));
            int s = Integer.parseInt(matcher.group(2));
            int l = Integer.parseInt(matcher.group(3));
            int[] rgb = hslToRgb(h, s / 100.0f, l / 100.0f);
            String rgbString = "rgb(" + rgb[0] + ", " + rgb[1] + ", " + rgb[2] + ")";
            matcher.appendReplacement(sb, rgbString);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static int[] hslToRgb(int h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60.0f) % 2 - 1));
        float m = l - c / 2;
        float r = Math.clamp(255 * (c + m), 0, 255);
        float g = Math.clamp(255 * (x + m), 0, 255);
        float b = Math.clamp(255 * (m), 0, 255);

        if (h >= 60 && h < 120) {
            r = g;
            g = r;
        } else if (h >= 120 && h < 180) {
            g = r;
            b = g;
        } else if (h >= 180 && h < 240) {
            b = r;
        } else if (h >= 240 && h < 300) {
            r = g;
            b = r;
        } else if (h >= 300 && h < 360) {
            b = g;
        }
        return new int[]{(int) r, (int) g, (int) b};
    }

    private String removeDocusignDiv(String htmlContent) {
        log.info("removing docusign divs");
        Document document = Jsoup.parse(htmlContent);
        Elements divsToRemove = document.getElementsByAttribute("docusign");

        for (Element div : divsToRemove) {
            String classNames = div.attr("class").toLowerCase();

            String width = "auto";
            String height = "auto";
            String lineHeight = "";
            String verticalAlignment = "";

            switch (classNames) {
                case "signature":
                    width = "64px";
                    height = "48px";
                    lineHeight = "38px";
                    verticalAlignment = "middle";
                    break;
                case "datesigned":
                    width = "54px";
                    height = "27px";
                    break;
                case "checkbox":
                    width = "18px";
                    height = "18px";
                    break;
                case "textarea":
                    width = "231px";
                    height = "52px";
                    lineHeight = "48px";
                    verticalAlignment = "middle";
                    break;
                default:
                    // No action needed for other class names
                    break;
            }

            Element placeholder = new Element("span")
                    .attr("style", String.format(
                            "display:inline-block; align-items: center; width:%s; height:%s; min-width:%s; min-height:%s; vertical-align:%s; line-height: %s",
                            width, height, width, height, verticalAlignment, lineHeight))
                    .html("&nbsp;");

            div.replaceWith(placeholder);
        }
        return document.html();
    }

    private String combineHtmlWithCss(String htmlContent, MultipartFile[] files) throws Exception {
        try{
            String cssContent = readFromFile(files[0]);         //file[0] is frontendcss
            String additionalStyle = readFromFile(files[1]);    //file[1] is additional css


            int styleTagStart = htmlContent.indexOf("<style>");
            int styleTagEnd = htmlContent.indexOf("</style>");
            if (styleTagStart == -1 || styleTagEnd == -1) {
                throw new Exception("HTML should contain <style> </style> tags");
            }

            String htmlTillStyleTag = htmlContent.substring(0, styleTagStart + 7);
            String htmlAfterStyleTag = htmlContent.substring(styleTagEnd);


            htmlContent = htmlContent.contains("<section")
                    ? htmlTillStyleTag + cssContent + additionalStyle + htmlAfterStyleTag
                    : htmlTillStyleTag + cssContent + htmlAfterStyleTag;
            return htmlContent
                    .replace("\n", "")
                    .replace("\r", "").replace(LESS_THAN_OPERATOR_REGEX_CONST,LESS_THAN_OPERATOR_CONST)
                    .replace(GREATER_THAN_OPERATOR_REGEX_CONST,GREATER_THAN_OPERATOR_CONST)
                    .replace("\\\"", "\"");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private String combineHtmlWithCssforHeaderFooter(String content, MultipartFile[] files) throws Exception {
        try{
            int styleTagStart = content.indexOf("<style>");
            int styleTagEnd = content.indexOf("</style>");

            if (styleTagStart == -1 || styleTagEnd == -1) {
                throw new Exception("HTML should contain <style> </style> tags");
            }

            String htmlTillStyleTag = content.substring(0, styleTagStart + 7);
            String htmlAfterStyleTag = content.substring(styleTagEnd);

            String cssContent = readFromFile(files[0]);     //file[0] is frontendcss
            String additionalStyle = readFromFile(files[2]);    //file[2] is headerfootercss

            return  htmlTillStyleTag + cssContent + additionalStyle + htmlAfterStyleTag;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private String readFromFile(MultipartFile file) throws Exception{
        try {
            if (file == null || file.isEmpty()) {
                return "";
            }
            return new String(file.getBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new Exception("Error reading file: " + e.getMessage());
        }
    }
}
