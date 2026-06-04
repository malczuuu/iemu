export interface ConnectionDTO {
  connected: boolean;
  endpoint: string | null;
  upstream: string | null;
  localPort: number;
  bootstrap: boolean;
  secureMode: boolean;
}
