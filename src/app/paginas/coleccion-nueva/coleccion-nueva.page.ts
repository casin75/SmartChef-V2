import { Component, computed, signal } from '@angular/core';
import { IonContent, IonInput, IonCheckbox, IonFooter, IonButton, NavController } from '@ionic/angular';
import { COLECCIONES } from '../../datos/colecciones';
import { RECETAS } from '../../datos/recetas';
import { CabeceraComponent } from '../../componentes/cabecera/cabecera.component';

@Component({
  selector: 'app-coleccion-nueva',
  templateUrl: './coleccion-nueva.page.html',
  styleUrls: ['./coleccion-nueva.page.scss'],
  imports: [IonContent, IonInput, IonCheckbox, IonFooter, IonButton, CabeceraComponent],
})
export class ColeccionNuevaPage {
  recetas = RECETAS;
  iconos = ['📌', '⚡', '🥗', '🥂', '🍝', '🎂', '🌱', '🔥'];

  // Lo que va rellenando el usuario (signals: al cambiar, se repinta solo lo que depende de ellas)
  nombre = signal('');
  icono = signal('📌');
  elegidas = signal<number[]>([]);

  // El botón Crear solo se activa si hay nombre
  sePuedeCrear = computed(() => this.nombre().trim().length > 0);

  constructor(private nav: NavController) {}

  marcar(id: number, marcada: boolean) {
    this.elegidas.update(ids => (marcada ? [...ids, id] : ids.filter(i => i !== id)));
  }

  crear() {
    if (!this.sePuedeCrear()) return;
    // Maqueta: la añadimos a la lista de datos. En la Entrega 2 será un POST a la API.
    COLECCIONES.push({
      id: Math.max(0, ...COLECCIONES.map(c => c.id)) + 1,
      nombre: this.nombre().trim(),
      icono: this.icono(),
      recetas: this.elegidas(),
    });
    this.nav.navigateBack('/app/favoritos');
  }
}
