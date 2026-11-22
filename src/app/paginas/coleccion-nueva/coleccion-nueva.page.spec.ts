import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ColeccionNuevaPage } from './coleccion-nueva.page';

describe('ColeccionNuevaPage', () => {
  let component: ColeccionNuevaPage;
  let fixture: ComponentFixture<ColeccionNuevaPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(ColeccionNuevaPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
