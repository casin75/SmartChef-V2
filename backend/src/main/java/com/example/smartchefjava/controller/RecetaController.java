package com.example.smartchefjava.controller;

import com.example.smartchefjava.dto.request.RecetaRequest;
import com.example.smartchefjava.dto.response.RecetaDetalleResponse;
import com.example.smartchefjava.dto.response.RecetaResumenResponse;
import com.example.smartchefjava.service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Recetas", description = "Catálogo de recetas")
@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
public class RecetaController {

  private final RecetaService recetaService;

  // GET /api/v1/recetas?nombre=tortilla&page=0&size=10&sort=tiempo,asc
  @Operation(summary = "Listar recetas, con filtro por nombre y paginación")
  @GetMapping
  public Page<RecetaResumenResponse> listar(
    @Parameter(description = "Parte del nombre", example = "tortilla")
    @RequestParam(required = false) String nombre,
    @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
    return recetaService.listar(nombre, pageable);
  }

  // GET /api/v1/recetas/7
  @Operation(summary = "Ver la ficha completa de una receta")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Receta encontrada"),
    @ApiResponse(responseCode = "404", description = "No existe esa receta", content = @Content)
  })
  @GetMapping("/{id}")
  public RecetaDetalleResponse buscar(
    @Parameter(description = "Id de la receta", example = "1")
    @PathVariable Integer id) {
    return recetaService.buscarPorId(id);
  }

  // POST /api/v1/recetas
  @Operation(summary = "Crear una receta")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Receta creada"),
    @ApiResponse(responseCode = "400", description = "Datos no válidos", content = @Content),
    @ApiResponse(responseCode = "404", description = "Algún ingrediente no existe", content = @Content),
    @ApiResponse(responseCode = "409", description = "Ya existe una receta con ese nombre", content = @Content),
    @ApiResponse(responseCode = "422", description = "Ingredientes repetidos", content = @Content)
  })
  @PostMapping
  public ResponseEntity<RecetaDetalleResponse> crear(@Valid @RequestBody RecetaRequest peticion) {

    RecetaDetalleResponse creada = recetaService.crear(peticion);

    URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(creada.id())
      .toUri();

    return ResponseEntity.created(ubicacion).body(creada);    // 201 + cabecera Location
  }

  // PUT /api/v1/recetas/7
  @Operation(summary = "Editar una receta")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Receta actualizada"),
    @ApiResponse(responseCode = "400", description = "Datos no válidos", content = @Content),
    @ApiResponse(responseCode = "404", description = "No existe la receta o algún ingrediente", content = @Content),
    @ApiResponse(responseCode = "409", description = "Ya existe otra receta con ese nombre", content = @Content),
    @ApiResponse(responseCode = "422", description = "Ingredientes repetidos", content = @Content)
  })
  @PutMapping("/{id}")
  public RecetaDetalleResponse actualizar(@PathVariable Integer id,
                                          @Valid @RequestBody RecetaRequest peticion) {
    return recetaService.actualizar(id, peticion);
  }

  // DELETE /api/v1/recetas/7
  @Operation(summary = "Eliminar una receta")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Receta eliminada"),
    @ApiResponse(responseCode = "404", description = "No existe esa receta", content = @Content),
    @ApiResponse(responseCode = "409", description = "Ya está en el historial de algún usuario", content = @Content)
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)                       // 204
  public void eliminar(@PathVariable Integer id) {
    recetaService.eliminar(id);
  }
}
