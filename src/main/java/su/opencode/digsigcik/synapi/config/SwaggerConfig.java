package su.opencode.digsigcik.synapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfoMetaData());
    }

    private ApiInfo apiInfoMetaData() {

        return new ApiInfoBuilder().title("API Documentation")
                .description("Tasks: https://docs.google.com/spreadsheets/d/1Ar7tm0XzfUVps_dgAeHr3h2oxnoTOY88QKNwPBGYxxg/edit#gid=864053792")
                .contact(new Contact("Dev-Team", "https://www.o-code.ru/", "vereschagina@o-code.ru"))
                .version("SEP0002, SEP0003, SEP0009, SEP0011, SEP0013, SEP0017, SEP0018, SEP0019, SEP0020, SEP0021, SEP0022, " +
                        "SEP0023, SEP0040, SEP0045, SEP0046, SEP0077, SEP0078, SEP0079, SEP0080, SEP0081, SEP0082, SEP0083, SEP0084, SEP0085, SEP0086")
                .build();
    }
}