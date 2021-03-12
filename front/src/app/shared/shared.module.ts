import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StateDisplayComponent } from './components/state-display/state-display.component';

@NgModule({
  declarations: [StateDisplayComponent],
  imports: [CommonModule],
  exports: [StateDisplayComponent],
})
export class SharedModule {}
