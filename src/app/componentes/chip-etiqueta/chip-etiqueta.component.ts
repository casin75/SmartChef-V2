import { Component, input } from '@angular/core';
import { Etiqueta } from '../../modelos/receta';

@Component({
  selector: 'app-chip-etiqueta',
  templateUrl: './chip-etiqueta.component.html',
  styleUrls: ['./chip-etiqueta.component.scss'],
})
export class ChipEtiquetaComponent {
  etiqueta = input.required<Etiqueta>();
}
