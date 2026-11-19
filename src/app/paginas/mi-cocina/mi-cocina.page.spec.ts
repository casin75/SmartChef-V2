import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MiCocinaPage } from './mi-cocina.page';

describe('MiCocinaPage', () => {
  let component: MiCocinaPage;
  let fixture: ComponentFixture<MiCocinaPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(MiCocinaPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
