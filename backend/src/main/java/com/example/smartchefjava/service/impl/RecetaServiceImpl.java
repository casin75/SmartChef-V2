package com.example.smartchefjava.service.impl;

import com.example.smartchefjava.dto.request.IngredienteRecetaRequest;
import com.example.smartchefjava.dto.request.RecetaRequest;
import com.example.smartchefjava.dto.response.RecetaDetalleResponse;
import com.example.smartchefjava.dto.response.RecetaResumenResponse;
import com.example.smartchefjava.exception.ConflictoException;
import com.example.smartchefjava.exception.RecursoNoEncontradoException;
import com.example.smartchefjava.exception.ReglaNegocioException;
import com.example.smartchefjava.mapper.RecetaMapper;
import com.example.smartchefjava.modelos.Foto;
import com.example.smartchefjava.modelos.Ingrediente;
import com.example.smartchefjava.modelos.Paso;
import com.example.smartchefjava.modelos.Receta;
import com.example.smartchefjava.modelos.RecetaIngrediente;
import com.example.smartchefjava.repository.IngredienteRepository;
import com.example.smartchefjava.repository.RecetaRepository;
import com.example.smartchefjava.repository.UsuarioRecetaRepository;
import com.example.smartchefjava.service.RecetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)   // por defecto todo es lectura; los métodos que escriben lo sobrescriben
public class RecetaServiceImpl implements RecetaService {

  private final RecetaRepository recetaRepository;
  private final IngredienteRepository ingredienteRepository;
  private final UsuarioRecetaRepository usuarioRecetaRepository;
  private final RecetaMapper recetaMapper;

  @Override
  public Page<RecetaResumenResponse> listar(String nombre, Pageable pageable) {
    Page<Receta> recetas = (nombre == null || nombre.isBlank())
      ? recetaRepository.findAll(pageable)
      : recetaRepository.findByNombreContainingIgnoreCase(nombre.trim(), pageable);

    return recetas.map(recetaMapper::aResumen);   // conserva el total de páginas
  }

  @Override
  public RecetaDetalleResponse buscarPorId(Integer id) {
    Receta receta = cargarDetalle(id);
    return recetaMapper.aDetalle(receta);
  }

  @Override
  @Transactional                                     // esta sí escribe
  public RecetaDetalleResponse crear(RecetaRequest peticion) {

    // 1. Reglas de negocio ANTES de tocar nada
    String nombre = peticion.nombre().trim();
    if (recetaRepository.existsByNombreIgnoreCase(nombre)) {
      throw new ConflictoException("Ya existe una receta llamada «" + nombre + "»");
    }
    comprobarIngredientesRepetidos(peticion.ingredientes());

    // 2. Construir la entidad: lo que el mapper ignoró lo ponemos aquí
    Receta receta = recetaMapper.aEntidad(peticion);
    receta.setNombre(nombre);
    ponerPasos(receta, peticion.pasos());
    ponerIngredientes(receta, peticion.ingredientes());
    ponerFotos(receta, peticion.fotos());

    // 3. Guardar y devolver
    Receta guardada = recetaRepository.save(receta);
    log.info("Receta creada: id={} nombre={}", guardada.getId(), guardada.getNombre());
    return recetaMapper.aDetalle(guardada);
  }

  @Override
  @Transactional
  public RecetaDetalleResponse actualizar(Integer id, RecetaRequest peticion) {
    Receta receta = cargarDetalle(id);

    // El nombre puede repetirse solo si es el de la propia receta
    String nombre = peticion.nombre().trim();
    if (recetaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
      throw new ConflictoException("Ya existe otra receta llamada «" + nombre + "»");
    }
    comprobarIngredientesRepetidos(peticion.ingredientes());

    recetaMapper.actualizar(receta, peticion);     // modifica la entidad en sitio
    receta.setNombre(nombre);

    if (peticion.pasos() != null) {
      receta.getPasos().clear();                 // orphanRemoval borra los antiguos
      ponerPasos(receta, peticion.pasos());
    }
    if (peticion.ingredientes() != null) {
      cambiarIngredientes(receta, peticion.ingredientes());
    }
    if (peticion.fotos() != null) {
      receta.getFotos().clear();
      ponerFotos(receta, peticion.fotos());
    }

    log.info("Receta actualizada: id={}", id);
    return recetaMapper.aDetalle(receta);          // no hace falta save(): la entidad está gestionada
  }

  @Override
  @Transactional
  public void eliminar(Integer id) {
    Receta receta = recetaRepository.findById(id)
      .orElseThrow(() -> new RecursoNoEncontradoException("No existe la receta con id " + id));

    // Si ya está en el historial de alguien, borrarla le dejaría el historial a medias
    if (usuarioRecetaRepository.existsByRecetaId(id)) {
      throw new ConflictoException("No se puede eliminar «" + receta.getNombre()
        + "»: ya aparece en el historial de algún usuario");
    }

    recetaRepository.delete(receta);
    log.info("Receta eliminada: id={}", id);
  }

  // ---------- Métodos privados de ayuda ----------

  // Trae la receta con sus pasos y sus ingredientes (JOIN FETCH, sin N+1)
  private Receta cargarDetalle(Integer id) {
    Receta receta = recetaRepository.buscarDetalleConPasos(id)
      .orElseThrow(() -> new RecursoNoEncontradoException("No existe la receta con id " + id));
    recetaRepository.buscarDetalleConIngredientes(id);   // rellena los ingredientes de la misma receta
    return receta;
  }

  // Una receta no puede llevar el mismo ingrediente dos veces
  private void comprobarIngredientesRepetidos(List<IngredienteRecetaRequest> ingredientes) {
    if (ingredientes == null) {
      return;
    }
    List<Integer> vistos = new ArrayList<>();
    for (IngredienteRecetaRequest ingrediente : ingredientes) {
      if (vistos.contains(ingrediente.idIngrediente())) {
        throw new ReglaNegocioException(
          "El ingrediente " + ingrediente.idIngrediente() + " está repetido en la receta");
      }
      vistos.add(ingrediente.idIngrediente());
    }
  }

  // Busca un ingrediente o lanza un 404 si no existe
  private Ingrediente buscarIngrediente(Integer id) {
    return ingredienteRepository.findById(id)
      .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ingrediente con id " + id));
  }

  // Los pasos llegan como textos en orden: el número se lo ponemos nosotros
  private void ponerPasos(Receta receta, List<String> pasos) {
    if (pasos == null) {
      return;
    }
    int numero = 1;
    for (String texto : pasos) {
      Paso paso = Paso.builder()
        .numero(numero)
        .descripcion(texto.trim())
        .receta(receta)
        .build();
      receta.getPasos().add(paso);
      numero++;
    }
  }

  private void ponerIngredientes(Receta receta, List<IngredienteRecetaRequest> pedidos) {
    if (pedidos == null) {
      return;
    }
    for (IngredienteRecetaRequest pedido : pedidos) {
      RecetaIngrediente recetaIngrediente = RecetaIngrediente.builder()
        .receta(receta)
        .ingrediente(buscarIngrediente(pedido.idIngrediente()))
        .cantidad(pedido.cantidad())
        .build();
      receta.getIngredientes().add(recetaIngrediente);
    }
  }

  // Al editar no se puede borrar y volver a crear la misma fila (tiene clave compuesta):
  // se quitan los que ya no vienen, se cambia la cantidad de los que siguen y se añaden los nuevos
  private void cambiarIngredientes(Receta receta, List<IngredienteRecetaRequest> pedidos) {
    List<Integer> idsPedidos = new ArrayList<>();
    for (IngredienteRecetaRequest pedido : pedidos) {
      idsPedidos.add(pedido.idIngrediente());
    }

    // 1. Quitar los que ya no están en la petición
    receta.getIngredientes().removeIf(ri -> !idsPedidos.contains(ri.getIngrediente().getId()));

    // 2. Cambiar la cantidad de los que siguen, o añadir los nuevos
    for (IngredienteRecetaRequest pedido : pedidos) {
      RecetaIngrediente existente = null;
      for (RecetaIngrediente ri : receta.getIngredientes()) {
        if (ri.getIngrediente().getId().equals(pedido.idIngrediente())) {
          existente = ri;
        }
      }

      if (existente != null) {
        existente.setCantidad(pedido.cantidad());
      } else {
        receta.getIngredientes().add(RecetaIngrediente.builder()
          .receta(receta)
          .ingrediente(buscarIngrediente(pedido.idIngrediente()))
          .cantidad(pedido.cantidad())
          .build());
      }
    }
  }

  private void ponerFotos(Receta receta, List<String> fotos) {
    if (fotos == null) {
      return;
    }
    for (String url : fotos) {
      Foto foto = Foto.builder()
        .url(url.trim())
        .receta(receta)
        .build();
      receta.getFotos().add(foto);
    }
  }
}
