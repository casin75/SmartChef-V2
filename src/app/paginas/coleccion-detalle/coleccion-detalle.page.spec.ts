import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ColeccionDetallePage } from './coleccion-detalle.page';

describe('ColeccionDetallePage', () => {
  let component: ColeccionDetallePage;
  let fixture: ComponentFixture<ColeccionDetallePage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(ColeccionDetallePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
