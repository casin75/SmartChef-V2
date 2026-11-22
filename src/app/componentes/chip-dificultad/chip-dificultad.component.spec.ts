import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChipDificultadComponent } from './chip-dificultad.component';

describe('ChipDificultadComponent', () => {
  let component: ChipDificultadComponent;
  let fixture: ComponentFixture<ChipDificultadComponent>;

  beforeEach(() => {
    fixture = TestBed.createComponent(ChipDificultadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
