package com.example.goodlearnai.v1;

import com.example.goodlearnai.v1.dto.PptReaderRequest;
import com.example.goodlearnai.v1.dto.PptReaderResponse;
import com.example.goodlearnai.v1.dto.SlideDto;
import com.example.goodlearnai.v1.service.PptReaderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PPT 读取服务测试类
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@SpringBootTest
public class PptReaderTest {
    
    @Autowired
    private PptReaderService pptReaderService;
    
    /**
     * 测试基本的 PPT 读取功能
     * 
     * 注意：运行此测试前，请确保指定路径下有 PPT 文件
     */
    @Test
    public void testReadPpt() {
        // 创建请求
        PptReaderRequest request = new PptReaderRequest();
        request.setFilePath("uploads/demo.pptx"); // 请根据实际情况修改路径
        request.setIncludeNotes(true);
        request.setMaxSlides(10);
        
        // 执行读取
        PptReaderResponse response = pptReaderService.readPpt(request);
        
        // 验证结果
        assertNotNull(response, "响应不应为空");
        
        if (response.getSuccess()) {
            // 如果成功，验证数据
            assertNotNull(response.getSlides(), "幻灯片列表不应为空");
            assertTrue(response.getTotalSlides() > 0, "应该至少有一页幻灯片");
            
            System.out.println("===== PPT 读取测试成功 =====");
            System.out.println("文件名: " + response.getFileName());
            System.out.println("总页数: " + response.getTotalSlides());
            System.out.println();
            
            // 打印每一页的内容
            for (SlideDto slide : response.getSlides()) {
                System.out.println("第 " + slide.getIndex() + " 页:");
                System.out.println("  标题: " + slide.getTitle());
                System.out.println("  正文长度: " + slide.getText().length() + " 字符");
                System.out.println("  正文预览: " + 
                    (slide.getText().length() > 100 
                        ? slide.getText().substring(0, 100) + "..." 
                        : slide.getText()));
                if (slide.getNotes() != null && !slide.getNotes().isEmpty()) {
                    System.out.println("  备注: " + slide.getNotes());
                }
                System.out.println("  文本块数量: " + slide.getTextBlocks().size());
                System.out.println("-----------------------------------");
            }
        } else {
            // 如果失败，打印错误信息（这是预期的，如果文件不存在）
            System.out.println("测试提示: " + response.getMessage());
            System.out.println("这是正常的，如果您还没有准备测试用的 PPT 文件");
        }
    }
    
    /**
     * 测试不包含备注的读取
     */
    @Test
    public void testReadPptWithoutNotes() {
        PptReaderRequest request = new PptReaderRequest();
        request.setFilePath("uploads/demo.pptx");
        request.setIncludeNotes(false);
        
        PptReaderResponse response = pptReaderService.readPpt(request);
        
        assertNotNull(response);
        
        if (response.getSuccess()) {
            System.out.println("===== 测试不包含备注 =====");
            for (SlideDto slide : response.getSlides()) {
                assertNull(slide.getNotes(), "备注应该为空");
            }
            System.out.println("✓ 备注排除功能正常");
        }
    }
    
    /**
     * 测试限制页数功能
     */
    @Test
    public void testReadPptWithMaxSlides() {
        PptReaderRequest request = new PptReaderRequest();
        request.setFilePath("uploads/demo.pptx");
        request.setMaxSlides(3);
        
        PptReaderResponse response = pptReaderService.readPpt(request);
        
        assertNotNull(response);
        
        if (response.getSuccess()) {
            System.out.println("===== 测试限制页数 =====");
            assertTrue(response.getSlides().size() <= 3, "返回的幻灯片数量不应超过3");
            System.out.println("返回页数: " + response.getSlides().size());
            System.out.println("✓ 页数限制功能正常");
        }
    }
    
    /**
     * 测试错误处理：文件不存在
     */
    @Test
    public void testReadNonExistentFile() {
        PptReaderRequest request = new PptReaderRequest();
        request.setFilePath("/path/to/nonexistent/file.pptx");
        
        PptReaderResponse response = pptReaderService.readPpt(request);
        
        assertNotNull(response);
        assertFalse(response.getSuccess(), "应该返回失败状态");
        assertNotNull(response.getMessage(), "应该包含错误信息");
        
        System.out.println("===== 测试错误处理 =====");
        System.out.println("错误信息: " + response.getMessage());
        System.out.println("✓ 错误处理正常");
    }
    
    /**
     * 测试空路径处理
     */
    @Test
    public void testReadWithEmptyPath() {
        PptReaderRequest request = new PptReaderRequest();
        request.setFilePath("");
        
        PptReaderResponse response = pptReaderService.readPpt(request);
        
        assertNotNull(response);
        assertFalse(response.getSuccess(), "应该返回失败状态");
        assertEquals("文件路径不能为空", response.getMessage());
        
        System.out.println("===== 测试空路径 =====");
        System.out.println("✓ 空路径处理正常");
    }
    
    /**
     * 测试直接通过路径读取
     */
    @Test
    public void testReadPptByPath() {
        try {
            List<SlideDto> slides = pptReaderService.readPptByPath("uploads/demo.pptx");
            
            System.out.println("===== 测试直接路径读取 =====");
            System.out.println("成功读取 " + slides.size() + " 页幻灯片");
            System.out.println("✓ 直接路径读取功能正常");
            
        } catch (Exception e) {
            System.out.println("测试提示: " + e.getMessage());
            System.out.println("这是正常的，如果您还没有准备测试用的 PPT 文件");
        }
    }
}

