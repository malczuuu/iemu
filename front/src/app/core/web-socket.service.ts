import {Injectable} from '@angular/core';
import {Subject, Observable} from 'rxjs';
import {environment} from '../../environments/environment';

@Injectable()
export class WebSocketService {

  private readonly api;

  private messages: Subject<any> = new Subject<any>();

  private socket: WebSocket;

  public constructor() {
    const schema = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    const endpoint = environment.production ? window.location.host + '/api' : environment.api.replace('http://', '');
    this.api = schema + endpoint + '/websocket';

    this.connect();

    setInterval(() => {
      if (this.socket === null) {
        this.connect();
      }
    }, 5000);
  }

  private connect(): void {
    this.socket = new WebSocket(this.api);
    this.socket.onopen = event => null;
    this.socket.onmessage = event => this.messages.next(event.data);
    this.socket.onclose = event => this.socket = null;
    this.socket.onerror = event => this.socket.close();
  }

  public onMessage(): Observable<any> {
    return this.messages.asObservable();
  }
}
