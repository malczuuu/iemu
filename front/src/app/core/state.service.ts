import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StateDTO, StatePatchDTO } from './state.model';

@Injectable()
export class StateService {
  private readonly api = '/api/state';

  public constructor(private http: HttpClient) {}

  public getState(): Observable<StateDTO> {
    return this.http.get<StateDTO>(this.api);
  }

  public patchState(patch: StatePatchDTO): Observable<void> {
    return this.http.patch<void>(this.api, patch);
  }
}
