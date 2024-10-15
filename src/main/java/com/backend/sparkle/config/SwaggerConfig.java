package com.backend.sparkle.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    //http://localhost:8080/swagger-ui/index.html 로 접속

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("프리캡스톤 sparkle swagger")
                        .version("1.0")
                        .description("프리캡스톤 sparkle swagger 입니다."));
    }
}