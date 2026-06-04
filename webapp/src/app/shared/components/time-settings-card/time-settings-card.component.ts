import { Component, Input } from '@angular/core';
import { StateDTO } from '../../../state/models/state.model';

@Component({
  selector: 'app-time-settings-card',
  templateUrl: './time-settings-card.component.html',
  styleUrls: ['./time-settings-card.component.scss'],
})
export class TimeSettingsCardComponent {
  @Input() public state!: StateDTO;
}
