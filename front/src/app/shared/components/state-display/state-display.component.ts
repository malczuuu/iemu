import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { StateDTO } from '../../../core/state.model';

@Component({
  selector: 'app-state-display',
  templateUrl: './state-display.component.html',
  styleUrls: ['./state-display.component.scss']
})
export class StateDisplayComponent implements OnInit {

  @Input()
  public state: StateDTO;

  @Output()
  public onOffToggle: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Output()
  public dimmerSave: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  public onTimeReset: EventEmitter<number> = new EventEmitter<number>();

  public constructor() {
  }

  public ngOnInit(): void {
  }

}
