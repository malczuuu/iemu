export interface ErrorDTO {
  code: number;
  message: string;
}

export interface StateDTO {
  deviceType: string;
  currentTime: string;
  timeZone: string;
  utcOffset: string;

  errors: ErrorDTO[];

  on: boolean;
  dimmer: number;
  onTime: number;
}

export interface StatePatchDTO {
  currentTime?: string;
  timeZone?: string;
  utcOffset?: string;

  on?: boolean;
  dimmer?: number;
  onTime?: number;
}
