package com.example.smartchefjava.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Datos generales que salen arriba en la página de Swagger
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI apiSmartChef() {
    return new OpenAPI().info(new Info()
      .title("API de SmartChef")
      .version("1.0")
      .description("Recetas, despensa y lista de la compra: come mejor, gasta menos"));
  }
}
