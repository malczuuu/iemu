import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { StateDTO } from '../../../core/state.model';
import { FirmwareDTO } from '../../../core/firmware.model';

@Component({
  selector: 'app-state-display',
  templateUrl: './state-display.component.html',
  styleUrls: ['./state-display.component.scss'],
  standalone: false,
})
export class StateDisplayComponent implements OnInit {
  @Input()
  public state: StateDTO;

  @Input()
  public firmware: FirmwareDTO;

  @Output()
  public onOffToggle: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Output()
  public dimmerSave: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  public onTimeReset: EventEmitter<number> = new EventEmitter<number>();

  public constructor() {}

  public ngOnInit(): void {}

  trimmedChecksum(): string {
    return this.firmware.fileChecksum.substring(0, 32) + '...';
  }

  trimmedUri(): string {
    let packageUri = this.firmware.packageUri;
    if (packageUri.length > 32) {
      const trimm = packageUri.length - 32;
      packageUri = '...' + packageUri.substring(trimm);
    }
    return packageUri;
  }
}
