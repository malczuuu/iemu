import { Component, Input, ChangeDetectionStrategy, signal } from '@angular/core';
import { FirmwareService } from '../../../core/services/firmware.service';
import { FirmwareDTO } from '../../../state/models/firmware.model';

@Component({
  selector: 'app-firmware-update-card',
  templateUrl: './firmware-update-card.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./firmware-update-card.component.scss'],
})
export class FirmwareUpdateCardComponent {
  @Input() public firmware!: FirmwareDTO;

  public stageError = signal<string | null>(null);
  public isStaging = signal(false);

  constructor(private firmwareService: FirmwareService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.stageError.set(null);
    this.isStaging.set(true);

    this.firmwareService.stageFirmware(file).subscribe({
      next: () => {
        this.isStaging.set(false);
        input.value = '';
      },
      error: (err) => {
        this.isStaging.set(false);
        this.stageError.set(
          err.error?.detail ?? 'File must contain a single hex color (#RRGGBB)',
        );
        input.value = '';
      },
    });
  }

  onExecute(): void {
    this.firmwareService.executeFirmwareUpdate().subscribe();
  }
}
