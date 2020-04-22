package org.truenewx.tnxjeex.fss.web.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class FssWebConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(100)); // 单个文件最大100MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(1000)); // 一次请求最大1000MB
        return factory.createMultipartConfig();
    }

}
