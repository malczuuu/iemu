import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { StateDisplayComponent } from './state-display.component';

describe('StateDisplayComponent', () => {
  let component: StateDisplayComponent;
  let fixture: ComponentFixture<StateDisplayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StateDisplayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StateDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
