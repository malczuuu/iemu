import { CommonModule } from '@angular/common';
import {
  Component,
  OnDestroy,
  OnInit,
  Signal,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { forkJoin, Subscription } from 'rxjs';
import { LwM2mClientService } from './core/services/lwm2m-client.service';
import { FirmwareService } from './core/services/firmware.service';
import { StateService } from './core/services/state.service';
import { Theme, ThemeService } from './core/services/theme.service';
import { WebSocketService } from './core/services/web-socket.service';
import { StateDisplayComponent } from './shared/components/state-display/state-display.component';
import { ConnectionDTO } from './state/models/connection.model';
import { FirmwareDTO } from './state/models/firmware.model';
import { StateDTO, StatePatchDTO } from './state/models/state.model';

@Component({
  selector: 'app-root',
  imports: [CommonModule, StateDisplayComponent],
  templateUrl: './app.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./app.component.scss'],
  // providers: [provideHttpClient(withInterceptorsFromDi())],
})
export class AppComponent implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  public state = signal<StateDTO | null>(null);
  public firmware = signal<FirmwareDTO | null>(null);
  public connection = signal<ConnectionDTO | null>(null);
  public theme: Signal<Theme>;

  public constructor(
    private stateService: StateService,
    private firmwareService: FirmwareService,
    private lwM2mClientService: LwM2mClientService,
    private webSocketService: WebSocketService,
    private themeService: ThemeService,
  ) {
    this.theme = this.themeService.theme();
  }

  public ngOnInit(): void {
    forkJoin({
      initialState: this.stateService.getState(),
      initialFirmware: this.firmwareService.getFirmware(),
      initialConnection: this.lwM2mClientService.getConnection(),
    }).subscribe(({ initialState, initialFirmware, initialConnection }) => {
      this.state.set(initialState);
      this.firmware.set(initialFirmware);
      this.connection.set(initialConnection);
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
      }),
    );
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    this.subscriptions = [];
  }

  public isStateReady(): boolean {
    return !!this.state() && !!this.firmware() && !!this.connection();
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

  public onConnect(): void {
    this.lwM2mClientService.connect().subscribe(() => {
      this.connection.update((c) => (c ? { ...c, connected: true } : c));
    });
  }

  public onDisconnect(): void {
    this.lwM2mClientService.disconnect().subscribe(() => {
      this.connection.update((c) => (c ? { ...c, connected: false } : c));
    });
  }

  public toggleTheme(): void {
    this.themeService.toggle();
  }
}
