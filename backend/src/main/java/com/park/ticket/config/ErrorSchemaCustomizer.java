package com.park.ticket.config;

import com.park.ticket.api.ApiError;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * springdoc 会把方法返回值填进每一个 @ApiResponse。
 * 这里把 4xx/5xx 改成 ApiError，避免错误响应当成订单或项目对象。
 */
@Component
public class ErrorSchemaCustomizer implements OpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new io.swagger.v3.oas.models.Components());
        }
        ModelConverters.getInstance()
                .readAll(new AnnotatedType(ApiError.class))
                .forEach(openApi.getComponents()::addSchemas);
        if (openApi.getPaths() == null) {
            return;
        }
        openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
            if (operation.getResponses() == null) {
                return;
            }
            operation.getResponses().forEach((code, response) -> {
                if (code == null || !code.chars().allMatch(Character::isDigit) || Integer.parseInt(code) < 400) {
                    return;
                }
                Schema<?> ref = new Schema<>();
                ref.set$ref("#/components/schemas/ApiError");
                MediaType mediaType = new MediaType();
                mediaType.setSchema(ref);
                response.setContent(new Content().addMediaType("application/json", mediaType));
            });
        }));
    }
}
