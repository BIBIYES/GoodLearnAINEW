package com.example.goodlearnai.v1.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Word文档解析工具类
 * 支持.doc和.docx格式的文档内容提取
 */
@Slf4j
public class WordDocumentParser {

    /**
     * 解析Word文档内容
     * @param file Word文档文件
     * @return 文档文本内容
     * @throws IOException 文件读取异常
     */
    public static String parseWordDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String content;
        try (InputStream inputStream = file.getInputStream()) {
            if (originalFilename.toLowerCase().endsWith(".docx")) {
                content = parseDocxDocument(inputStream);
            } else if (originalFilename.toLowerCase().endsWith(".doc")) {
                content = parseDocDocument(inputStream);
            } else {
                throw new IllegalArgumentException("不支持的文件格式，仅支持.doc和.docx格式");
            }
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("文档内容为空");
        }

        log.info("成功解析Word文档，内容长度: {} 字符", content.length());
        return content.trim();
    }

    /**
     * 解析.docx格式文档
     * @param inputStream 文件输入流
     * @return 文档内容
     * @throws IOException 读取异常
     */
    private static String parseDocxDocument(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            // 解析段落内容
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            
            // 解析表格内容
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                content.append("\n=== 表格内容 ===\n");
                parseXWPFTable(table, content);
                content.append("=== 表格结束 ===\n\n");
            }
        }
        
        return content.toString();
    }

    /**
     * 解析.doc格式文档
     * @param inputStream 文件输入流
     * @return 文档内容
     * @throws IOException 读取异常
     */
    private static String parseDocDocument(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            // 获取文档范围
            Range range = document.getRange();
            
            // 先提取基本文本内容
            try (WordExtractor extractor = new WordExtractor(document)) {
                String basicText = extractor.getText();
                if (basicText != null && !basicText.trim().isEmpty()) {
                    content.append(basicText).append("\n");
                }
            }
            
            // 解析表格内容
            TableIterator tableIterator = new TableIterator(range);
            while (tableIterator.hasNext()) {
                Table table = tableIterator.next();
                content.append("\n=== 表格内容 ===\n");
                parseHWPFTable(table, content);
                content.append("=== 表格结束 ===\n\n");
            }
        }
        
        return content.toString();
    }

    /**
     * 验证文件是否为Word文档
     * @param file 文件
     * @return 是否为Word文档
     */
    public static boolean isWordDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String lowerCaseFilename = originalFilename.toLowerCase();
        return lowerCaseFilename.endsWith(".doc") || lowerCaseFilename.endsWith(".docx");
    }

    /**
     * 解析XWPF表格内容（.docx格式）
     * @param table 表格对象
     * @param content 内容构建器
     */
    private static void parseXWPFTable(XWPFTable table, StringBuilder content) {
        List<XWPFTableRow> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                String cellText = cell.getText().trim();
                
                if (!cellText.isEmpty()) {
                    // 如果是第一列，可能是标题或标签
                    if (j == 0 && !cellText.matches("\\d+")) {
                        content.append("【").append(cellText).append("】: ");
                    } else {
                        content.append(cellText);
                        if (j < cells.size() - 1) {
                            content.append(" | ");
                        }
                    }
                }
            }
            content.append("\n");
        }
    }
    
    /**
     * 解析HWPF表格内容（.doc格式）
     * @param table 表格对象
     * @param content 内容构建器
     */
    private static void parseHWPFTable(Table table, StringBuilder content) {
        int numRows = table.numRows();
        for (int i = 0; i < numRows; i++) {
            TableRow row = table.getRow(i);
            int numCells = row.numCells();
            
            for (int j = 0; j < numCells; j++) {
                TableCell cell = row.getCell(j);
                String cellText = cell.text().trim();
                
                if (!cellText.isEmpty()) {
                    // 如果是第一列，可能是标题或标签
                    if (j == 0 && !cellText.matches("\\d+")) {
                        content.append("【").append(cellText).append("】: ");
                    } else {
                        content.append(cellText);
                        if (j < numCells - 1) {
                            content.append(" | ");
                        }
                    }
                }
            }
            content.append("\n");
        }
    }
    
    /**
     * 清理和格式化文档内容
     * @param content 原始内容
     * @return 清理后的内容
     */
    public static String cleanContent(String content) {
        if (content == null) {
            return "";
        }
        
        // 移除多余的空行和空格，但保留表格结构
        return content.replaceAll("\\n{3,}", "\n\n")
                     .replaceAll("[ \t]+", " ")
                     .trim();
    }
}