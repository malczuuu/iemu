import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FeaturesDTO } from '../../state/models/features.model';

@Injectable({
  providedIn: 'root',
})
export class FeaturesService {
  private readonly api = '/api/features';

  constructor(private http: HttpClient) {}

  public getFeatures(): Observable<FeaturesDTO> {
    return this.http.get<FeaturesDTO>(this.api);
  }
}
