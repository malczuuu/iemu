import { Component, EventEmitter, Input, Output } from '@angular/core';
import { StateDTO } from '../../../state/models/state.model';

@Component({
  selector: 'app-light-control-card',
  templateUrl: './light-control-card.component.html',
  styleUrls: ['./light-control-card.component.scss'],
})
export class LightControlCardComponent {
  @Input() public state!: StateDTO;

  @Output() public onOffToggle: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() public dimmerSave: EventEmitter<number> = new EventEmitter<number>();
  @Output() public onTimeReset: EventEmitter<number> = new EventEmitter<number>();

  onDimmerSave(value: string) {
    this.dimmerSave.emit(Number(value));
  }
}
