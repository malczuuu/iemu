import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { WebSocketService } from './web-socket.service';
import { StateService } from './state.service';

@NgModule({
  declarations: [],
  imports: [CommonModule],
  providers: [StateService, WebSocketService, provideHttpClient(withInterceptorsFromDi())],
})
export class CoreModule {}
