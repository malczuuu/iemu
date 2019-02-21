import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { StateDTO, StatePatchDTO } from './state.model';

@Injectable()
export class StateService {

  private readonly api = environment.api + '/state';

  public constructor(private http: HttpClient) {
  }

  public getState(): Observable<StateDTO> {
    return this.http.get<StateDTO>(this.api);
  }

  public patchState(patch: StatePatchDTO): Observable<void> {
    return this.http.patch<void>(this.api, patch);
  }

}
