import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { ConnectionDTO } from '../../../state/models/connection.model';

@Component({
  selector: 'app-connection-card',
  templateUrl: './connection-card.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./connection-card.component.scss'],
})
export class ConnectionCardComponent {
  @Input() public connection!: ConnectionDTO;

  @Output() public connect: EventEmitter<void> = new EventEmitter<void>();
  @Output() public disconnect: EventEmitter<void> = new EventEmitter<void>();
}
