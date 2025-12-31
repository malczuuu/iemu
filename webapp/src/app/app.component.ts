import { CommonModule } from '@angular/common';
import { Component, computed, OnDestroy, OnInit, Signal, signal } from '@angular/core';
import { forkJoin, Subscription } from 'rxjs';
import { FirmwareService } from './core/services/firmware.service';
import { StateService } from './core/services/state.service';
import { WebSocketService } from './core/services/web-socket.service';
import { StateDisplayComponent } from './shared/components/state-display/state-display.component';
import { FirmwareDTO } from './state/models/firmware.model';
import { StateDTO, StatePatchDTO } from './state/models/state.model';

@Component({
  selector: 'app-root',
  imports: [CommonModule, StateDisplayComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  // providers: [provideHttpClient(withInterceptorsFromDi())],
})
export class AppComponent implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  public state = signal<StateDTO | null>(null);
  public firmware = signal<FirmwareDTO | null>(null);

  public constructor(
    private stateService: StateService,
    private firmwareService: FirmwareService,
    private webSocketService: WebSocketService
  ) {}

  public ngOnInit(): void {
    forkJoin({
      initialState: this.stateService.getState(),
      initialFirmware: this.firmwareService.getFirmware(),
    }).subscribe(({ initialState, initialFirmware }) => {
      this.state.set(initialState);
      this.firmware.set(initialFirmware);
    });

    this.subscriptions.push(
      this.webSocketService.onMessage().subscribe((message) => {
        const event = JSON.parse(message);
        switch (event.type) {
          case 'state':
            this.state.set(event.body);
            break;
          case 'firmware':
            this.firmware.set(event.body);
        }
      })
    );
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    this.subscriptions = [];
  }

  public isStateReady(): boolean {
    return !!this.state() && !!this.firmware();
  }

  public onOffToggle(): void {
    const patch: StatePatchDTO = { on: !this.state()?.on };
    this.stateService.patchState(patch).subscribe(() => null);
  }

  public onDimmerChanged(dimmer: number): void {
    const patch: StatePatchDTO = { dimmer: dimmer };
    this.stateService.patchState(patch).subscribe(() => null);
  }

  public onOnTimeReset(): void {
    const patch: StatePatchDTO = { onTime: 0 };
    this.stateService.patchState(patch).subscribe(() => null);
  }
}
