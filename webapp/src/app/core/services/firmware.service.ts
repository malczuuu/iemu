import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { from, Observable, switchMap } from 'rxjs';
import { FirmwareDTO } from '../../state/models/firmware.model';

@Injectable({
  providedIn: 'root',
})
export class FirmwareService {
  private readonly api = '/api/firmware';

  constructor(private http: HttpClient) {}

  public getFirmware(): Observable<FirmwareDTO> {
    return this.http.get<FirmwareDTO>(this.api);
  }

  public stageFirmware(file: File): Observable<void> {
    return from(file.text()).pipe(
      switchMap((content) =>
        this.http.post<void>(this.api, content, {
          headers: { 'Content-Type': 'text/plain' },
        }),
      ),
    );
  }

  public executeFirmwareUpdate(): Observable<void> {
    return this.http.post<void>(`${this.api}/execute`, null);
  }
}
