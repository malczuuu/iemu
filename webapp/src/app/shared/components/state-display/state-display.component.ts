import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { StateDTO } from '../../../state/models/state.model';
import { FirmwareDTO } from '../../../state/models/firmware.model';
import { ConnectionDTO } from '../../../state/models/connection.model';

@Component({
  selector: 'app-state-display',
  templateUrl: './state-display.component.html',
  styleUrls: ['./state-display.component.scss'],
})
export class StateDisplayComponent implements OnInit {
  @Input()
  public state!: StateDTO;

  @Input()
  public firmware!: FirmwareDTO;

  @Input()
  public connection!: ConnectionDTO;

  @Output()
  public onOffToggle: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Output()
  public dimmerSave: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  public onTimeReset: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  public connect: EventEmitter<void> = new EventEmitter<void>();

  @Output()
  public disconnect: EventEmitter<void> = new EventEmitter<void>();

  public constructor() {}

  public ngOnInit(): void {}

  trimmedChecksum(): string {
    return this.firmware.fileChecksum.substring(0, 32) + '...';
  }

  trimmedUri(): string {
    let packageUri = this.firmware.packageUri;
    if (packageUri && packageUri.length > 32) {
      const trimm = packageUri.length - 32;
      packageUri = '...' + packageUri.substring(trimm);
    }
    return packageUri;
  }

  onDimmerSave(value: string) {
    this.dimmerSave.emit(Number(value));
  }
}
