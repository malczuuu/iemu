import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { FirmwareDTO } from '../../../state/models/firmware.model';

@Component({
  selector: 'app-firmware-update-card',
  templateUrl: './firmware-update-card.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./firmware-update-card.component.scss'],
})
export class FirmwareUpdateCardComponent {
  @Input() public firmware!: FirmwareDTO;

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
}
