import { Component, OnDestroy, OnInit } from '@angular/core';
import { WebSocketService } from './core/web-socket.service';
import { Subscription } from 'rxjs';
import { StateDTO, StatePatchDTO } from './core/state.model';
import { StateService } from './core/state.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription[] = [];

  public state: StateDTO;

  public constructor(private stateService: StateService, private webSocketService: WebSocketService) {
  }


  public ngOnInit(): void {
    this.stateService.getState().subscribe(state => {
      this.state = state;
      this.subscriptions.push(this.webSocketService.onMessage().subscribe(
        message => {
          const event = JSON.parse(message);
          if (event.type === 'state') {
            this.state = event.body;
          }
        }
      ));
    });
  }

  public ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.subscriptions = [];
  }

  public onOffToggle(): void {
    const patch: StatePatchDTO = { on: !this.state.on };
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
