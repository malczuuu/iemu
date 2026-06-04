import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConnectionDTO } from '../../state/models/connection.model';

@Injectable({
  providedIn: 'root',
})
export class LwM2mClientService {
  private readonly api = '/api/lwm2m-client';

  public constructor(private http: HttpClient) {}

  public getConnection(): Observable<ConnectionDTO> {
    return this.http.get<ConnectionDTO>(this.api);
  }

  public connect(): Observable<void> {
    return this.http.post<void>(this.api, null);
  }

  public disconnect(): Observable<void> {
    return this.http.delete<void>(this.api);
  }
}
