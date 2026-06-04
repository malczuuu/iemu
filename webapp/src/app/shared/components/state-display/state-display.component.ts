import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ConnectionDTO } from '../../../state/models/connection.model';
import { FirmwareDTO } from '../../../state/models/firmware.model';
import { StateDTO } from '../../../state/models/state.model';
import { ConnectionCardComponent } from '../connection-card/connection-card.component';
import { FirmwareUpdateCardComponent } from '../firmware-update-card/firmware-update-card.component';
import { LightControlCardComponent } from '../light-control-card/light-control-card.component';
import { TimeSettingsCardComponent } from '../time-settings-card/time-settings-card.component';

@Component({
  selector: 'app-state-display',
  imports: [
    ConnectionCardComponent,
    TimeSettingsCardComponent,
    LightControlCardComponent,
    FirmwareUpdateCardComponent,
  ],
  templateUrl: './state-display.component.html',
  styleUrls: ['./state-display.component.scss'],
})
export class StateDisplayComponent {
  @Input() public state!: StateDTO;
  @Input() public firmware!: FirmwareDTO;
  @Input() public connection!: ConnectionDTO;

  @Output() public onOffToggle: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() public dimmerSave: EventEmitter<number> = new EventEmitter<number>();
  @Output() public onTimeReset: EventEmitter<number> = new EventEmitter<number>();
  @Output() public connect: EventEmitter<void> = new EventEmitter<void>();
  @Output() public disconnect: EventEmitter<void> = new EventEmitter<void>();
}
