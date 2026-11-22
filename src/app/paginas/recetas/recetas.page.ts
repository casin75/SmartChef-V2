import { Component } from '@angular/core';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular';
import { TarjetaRecetaComponent } from '../../componentes/tarjeta-receta/tarjeta-receta.component';
import { RECETAS } from '../../datos/recetas';

@Component({
  selector: 'app-recetas',
  templateUrl: './recetas.page.html',
  styleUrls: ['./recetas.page.scss'],
  imports: [IonContent, IonHeader, IonTitle, IonToolbar, TarjetaRecetaComponent],
})
export class RecetasPage {
  recetas = RECETAS; // las 9 recetas de ejemplo
}
