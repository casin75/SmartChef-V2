import { Component, computed, input } from '@angular/core';
import { IonContent, IonBackButton, IonSegment, IonSegmentButton, IonLabel, IonIcon, IonButton,
  IonFooter, IonModal, IonCheckbox, NavController } from '@ionic/angular';
import { RECETAS } from '../../datos/recetas';
import { COLECCIONES, FAVORITAS_RECIENTES } from '../../datos/colecciones';
import { Coleccion } from '../../modelos/coleccion';
import { UNIDADES } from '../../modelos/ingrediente';
import { loTengo } from '../../utilidades/despensa';
import { ChipDificultadComponent } from '../../componentes/chip-dificultad/chip-dificultad.component';
import { ChipEtiquetaComponent } from '../../componentes/chip-etiqueta/chip-etiqueta.component';
import { EstadoVacioComponent } from '../../componentes/estado-vacio/estado-vacio.component';

@Component({
  selector: 'app-receta-detalle',
  templateUrl: './receta-detalle.page.html',
  styleUrls: ['./receta-detalle.page.scss'],
  imports: [IonContent, IonBackButton, IonSegment, IonSegmentButton, IonLabel, IonIcon, IonButton, IonFooter,
    IonModal, IonCheckbox,
    ChipDificultadComponent, ChipEtiquetaComponent, EstadoVacioComponent],
})
export class RecetaDetallePage {
  id = input<string>(); // recibe el ":id" de la dirección
  volver = input('/app/recetas'); // recibe el "data.volver" de la ruta: a dónde ir si no hay historial

  // Buscamos la receta cada vez que cambia el id. En la dirección todo es texto: lo pasamos a número.
  receta = computed(() => RECETAS.find(r => r.id === Number(this.id())));

  seccion: 'ingredientes' | 'pasos' = 'ingredientes';
  favorita = false;
  cocinada = false;

  unidades = UNIDADES;
  loTengo = loTengo;

  faltantes = computed(() => this.receta()?.ingredientes.filter(ri => !loTengo(ri.ingrediente.id)).length ?? 0);

  // Colecciones: la ventana de abajo para añadir o quitar la receta de cada una
  colecciones = COLECCIONES;
  verColecciones = false;

  constructor(private nav: NavController) {}

  // El icono se pinta relleno si la receta está en al menos una colección
  estaEnAlguna(): boolean {
    return this.colecciones.some(c => c.recetas.includes(Number(this.id())));
  }

  alternar(c: Coleccion, idReceta: number) {
    const pos = c.recetas.indexOf(idReceta);
    if (pos === -1) c.recetas.push(idReceta);
    else c.recetas.splice(pos, 1);
  }

  // Primero cerramos la ventana y, cuando ya se ha cerrado, vamos a crear la colección
  private irANueva = false;

  nuevaColeccion() {
    this.irANueva = true;
    this.verColecciones = false;
  }

  alCerrar() {
    this.verColecciones = false;
    if (this.irANueva) {
      this.irANueva = false;
      this.nav.navigateForward('/app/favoritos/nueva');
    }
  }

  ngOnInit() {
    this.favorita = FAVORITAS_RECIENTES.includes(Number(this.id()));
  }
}
