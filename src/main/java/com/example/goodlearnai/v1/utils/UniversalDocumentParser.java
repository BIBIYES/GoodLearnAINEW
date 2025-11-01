package com.example.goodlearnai.v1.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通用文档解析工具类
 * 支持多种文档格式：PPT、Word、Markdown、TXT等
 * 
 * @author DSfeiji
 * @since 2025-11-01
 */
@Slf4j
public class UniversalDocumentParser {
    
    /**
     * 支持的文件扩展名
     */
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "pptx", "ppt",           // PowerPoint
        "docx", "doc",           // Word
        "md", "markdown",        // Markdown
        "txt", "text"            // 纯文本
    ));
    
    /**
     * 文档类型枚举
     */
    public enum DocumentType {
        PPT("pptx", "ppt"),
        WORD("docx", "doc"),
        MARKDOWN("md", "markdown"),
        TEXT("txt", "text"),
        UNKNOWN();
        
        private final Set<String> extensions;
        
        DocumentType(String... exts) {
            this.extensions = new HashSet<>(Arrays.asList(exts));
        }
        
        public boolean matches(String extension) {
            return extensions.contains(extension.toLowerCase());
        }
        
        public static DocumentType fromExtension(String extension) {
            String ext = extension.toLowerCase();
            for (DocumentType type : values()) {
                if (type.matches(ext)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
    
    /**
     * 验证文件是否为支持的格式
     * 
     * @param file 文件
     * @return 是否支持
     */
    public static boolean isSupportedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(file);
        return extension != null && SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * 获取文件类型
     * 
     * @param file 文件
     * @return 文档类型
     */
    public static DocumentType getDocumentType(MultipartFile file) {
        String extension = getFileExtension(file);
        if (extension == null) {
            return DocumentType.UNKNOWN;
        }
        return DocumentType.fromExtension(extension);
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param file 文件
     * @return 扩展名（不含点）
     */
    public static String getFileExtension(MultipartFile file) {
        if (file == null) {
            return null;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return null;
        }
        
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
    
    /**
     * 解析文档内容（自动识别文件类型）
     * 
     * @param file 文档文件
     * @return 文档文本内容
     * @throws IOException 文件读取异常
     */
    public static String parseDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        DocumentType type = getDocumentType(file);
        log.info("开始解析文档，类型: {}, 文件名: {}", type, file.getOriginalFilename());
        
        String content;
        switch (type) {
            case WORD:
                content = parseWordDocument(file);
                break;
            case MARKDOWN:
                content = parseMarkdownFile(file);
                break;
            case TEXT:
                content = parseTextFile(file);
                break;
            case PPT:
                throw new IllegalArgumentException("PPT文件请使用专门的PPT解析服务");
            default:
                throw new IllegalArgumentException("不支持的文件格式: " + getFileExtension(file));
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("文档内容为空");
        }
        
        log.info("文档解析完成，内容长度: {} 字符", content.length());
        return content.trim();
    }
    
    /**
     * 解析Word文档内容
     * 
     * @param file Word文档文件
     * @return 文档文本内容
     * @throws IOException 文件读取异常
     */
    private static String parseWordDocument(MultipartFile file) throws IOException {
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
                throw new IllegalArgumentException("不支持的Word文件格式，仅支持.doc和.docx格式");
            }
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Word文档内容为空");
        }

        log.info("成功解析Word文档，内容长度: {} 字符", content.length());
        return content.trim();
    }
    
    /**
     * 解析.docx格式文档
     * 
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
     * 
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
     * 解析XWPF表格内容（.docx格式）
     * 
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
     * 
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
     * 解析Markdown文件
     * 
     * @param file Markdown文件
     * @return 文档内容
     * @throws IOException 读取异常
     */
    private static String parseMarkdownFile(MultipartFile file) throws IOException {
        log.info("解析Markdown文件: {}", file.getOriginalFilename());
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int headingLevel = 0;
            
            while ((line = reader.readLine()) != null) {
                // 处理Markdown格式
                if (line.startsWith("#")) {
                    // 标题
                    headingLevel = line.indexOf(' ');
                    String heading = line.substring(headingLevel + 1).trim();
                    content.append("\n").append("【").append(heading).append("】\n");
                } else if (line.startsWith("```")) {
                    // 代码块标记，保留
                    content.append(line).append("\n");
                } else if (line.startsWith("-") || line.startsWith("*") || line.startsWith("+")) {
                    // 列表项
                    content.append("  ").append(line.substring(1).trim()).append("\n");
                } else if (line.matches("^\\d+\\..*")) {
                    // 有序列表
                    content.append("  ").append(line.substring(line.indexOf('.') + 1).trim()).append("\n");
                } else if (!line.trim().isEmpty()) {
                    // 普通文本
                    content.append(line).append("\n");
                } else {
                    // 空行保留
                    content.append("\n");
                }
            }
        }
        
        // 清理Markdown语法符号
        String cleanedContent = content.toString()
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")  // 粗体
                .replaceAll("\\*(.+?)\\*", "$1")        // 斜体
                .replaceAll("__(.+?)__", "$1")          // 粗体
                .replaceAll("_(.+?)_", "$1")            // 斜体
                .replaceAll("~~(.+?)~~", "$1")          // 删除线
                .replaceAll("`(.+?)`", "$1")            // 行内代码
                .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1"); // 链接
        
        return cleanedContent;
    }
    
    /**
     * 解析纯文本文件
     * 
     * @param file 文本文件
     * @return 文档内容
     * @throws IOException 读取异常
     */
    private static String parseTextFile(MultipartFile file) throws IOException {
        log.info("解析文本文件: {}", file.getOriginalFilename());
        
        StringBuilder content = new StringBuilder();
        
        // 尝试多种编码
        String[] encodings = {
            StandardCharsets.UTF_8.name(),
            "GBK",
            "GB2312",
            StandardCharsets.ISO_8859_1.name()
        };
        
        Exception lastException = null;
        for (String encoding : encodings) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), encoding))) {
                
                content.setLength(0); // 清空之前的内容
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                
                // 检查是否有乱码（简单检测）
                String result = content.toString();
                if (!containsGarbledText(result)) {
                    log.info("使用编码 {} 成功解析文本文件", encoding);
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
                log.debug("使用编码 {} 解析失败", encoding);
            }
        }
        
        // 所有编码都失败
        if (lastException != null) {
            throw new IOException("无法解析文本文件，尝试了多种编码均失败", lastException);
        }
        
        return content.toString();
    }
    
    /**
     * 检测是否包含乱码
     * 
     * @param text 文本
     * @return 是否包含乱码
     */
    private static boolean containsGarbledText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 简单检测：如果包含大量问号或特殊字符，可能是乱码
        long questionMarks = text.chars().filter(ch -> ch == '?').count();
        long totalChars = text.length();
        
        // 如果问号超过10%，认为可能是乱码
        return totalChars > 0 && (questionMarks * 100.0 / totalChars) > 10;
    }
    
    /**
     * 获取支持的文件扩展名列表（用于显示）
     * 
     * @return 扩展名字符串，如 ".pptx, .docx, .md, .txt"
     */
    public static String getSupportedExtensions() {
        return String.join(", ", 
            SUPPORTED_EXTENSIONS.stream()
                .map(ext -> "." + ext)
                .sorted()
                .toArray(String[]::new)
        );
    }
    
    /**
     * 清理和格式化文档内容
     * 
     * @param content 原始内容
     * @return 清理后的内容
     */
    public static String cleanContent(String content) {
        if (content == null) {
            return "";
        }
        
        // 移除多余的空行和空格，但保留基本结构
        return content.replaceAll("\\n{3,}", "\n\n")
                     .replaceAll("[ \t]+", " ")
                     .replaceAll("(?m)^[ \t]+", "")  // 移除行首空格
                     .trim();
    }
}

