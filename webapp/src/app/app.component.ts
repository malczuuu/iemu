import { CommonModule } from '@angular/common';
import {
  Component,
  OnDestroy,
  OnInit,
  Signal,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { catchError, forkJoin, of, Subscription, switchMap } from 'rxjs';
import { LwM2mClientService } from './core/services/lwm2m-client.service';
import { FeaturesService } from './core/services/features.service';
import { FirmwareService } from './core/services/firmware.service';
import { StateService } from './core/services/state.service';
import { Theme, ThemeService } from './core/services/theme.service';
import { WebSocketService } from './core/services/web-socket.service';
import { StateDisplayComponent } from './shared/components/state-display/state-display.component';
import { ConnectionDTO } from './state/models/connection.model';
import { FeaturesDTO } from './state/models/features.model';
import { FirmwareDTO } from './state/models/firmware.model';
import { StateDTO, StatePatchDTO } from './state/models/state.model';

@Component({
  selector: 'app-root',
  imports: [CommonModule, StateDisplayComponent],
  templateUrl: './app.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  public state = signal<StateDTO | null>(null);
  public firmware = signal<FirmwareDTO | null>(null);
  public connection = signal<ConnectionDTO | null>(null);
  public features = signal<FeaturesDTO | null>(null);
  public theme: Signal<Theme>;

  public constructor(
    private stateService: StateService,
    private firmwareService: FirmwareService,
    private featuresService: FeaturesService,
    private lwM2mClientService: LwM2mClientService,
    private webSocketService: WebSocketService,
    private themeService: ThemeService,
  ) {
    this.theme = this.themeService.theme();
  }

  public ngOnInit(): void {
    forkJoin({
      initialState: this.stateService.getState(),
      initialConnection: this.lwM2mClientService.getConnection(),
      initialFeatures: this.featuresService.getFeatures(),
    })
      .pipe(
        switchMap(({ initialState, initialConnection, initialFeatures }) => {
          this.state.set(initialState);
          this.connection.set(initialConnection);
          this.features.set(initialFeatures);
          const firmware$ = initialFeatures.firmwareUpdate
            ? this.firmwareService.getFirmware().pipe(catchError(() => of(null)))
            : of(null);
          return firmware$;
        }),
      )
      .subscribe((firmware) => {
        this.firmware.set(firmware);
      });

    this.subscriptions.push(
      this.webSocketService.onMessage().subscribe((message) => {
        const event = JSON.parse(message);
        switch (event.type) {
          case 'state':
            this.state.set(event.body);
            break;
          case 'firmware':
            if (this.features()?.firmwareUpdate) {
              this.firmware.set(event.body);
            }
            break;
        }
      }),
    );
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    this.subscriptions = [];
  }

  public isStateReady(): boolean {
    return !!this.state() && !!this.connection() && this.features() !== null;
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
