export interface FirmwareDTO {
  fileChecksum: string;
  packageUri: string;
  state: string;
  stateValue: number;
  result: string;
  resultValue: number;
  pkgVersion: string;
  deliveryMethod: string;
  deliveryMethodValue: number;
  progress: number;
}
