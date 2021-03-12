import {Component, OnDestroy, OnInit} from '@angular/core';
import {WebSocketService} from './core/web-socket.service';
import {Subscription} from 'rxjs';
import {StateDTO, StatePatchDTO} from './core/state.model';
import {StateService} from './core/state.service';
import {FirmwareService} from './core/firmware.service';
import {FirmwareDTO} from './core/firmware.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription[] = [];

  public state: StateDTO;
  public firmware: FirmwareDTO;

  public constructor(
    private stateService: StateService,
    private firmwareService: FirmwareService,
    private webSocketService: WebSocketService) {
  }


  public ngOnInit(): void {
    this.stateService.getState().subscribe(state => this.state = state);
    this.firmwareService.getFirmware().subscribe(firmware => this.firmware = firmware);
    this.subscriptions.push(this.webSocketService.onMessage().subscribe(
      message => {
        const event = JSON.parse(message);
        switch (event.type) {
          case 'state':
            this.state = event.body;
            break;
          case 'firmware':
            this.firmware = event.body;
        }
      }
    ));
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.subscriptions = [];
  }

  public onOffToggle(): void {
    const patch: StatePatchDTO = {on: !this.state.on};
    this.stateService.patchState(patch).subscribe(() => null);
  }

  public onDimmerChanged(dimmer: number): void {
    const patch: StatePatchDTO = {dimmer: dimmer};
    this.stateService.patchState(patch).subscribe(() => null);
  }

  public onOnTimeReset(): void {
    const patch: StatePatchDTO = {onTime: 0};
    this.stateService.patchState(patch).subscribe(() => null);
  }
}
