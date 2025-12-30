import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FirmwareDTO } from './firmware.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class FirmwareService {
  private readonly api = '/api/firmware';

  constructor(private http: HttpClient) {}

  public getFirmware(): Observable<FirmwareDTO> {
    return this.http.get<FirmwareDTO>(this.api);
  }
}
