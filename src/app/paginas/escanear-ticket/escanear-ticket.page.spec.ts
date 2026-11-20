import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EscanearTicketPage } from './escanear-ticket.page';

describe('EscanearTicketPage', () => {
  let component: EscanearTicketPage;
  let fixture: ComponentFixture<EscanearTicketPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(EscanearTicketPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
