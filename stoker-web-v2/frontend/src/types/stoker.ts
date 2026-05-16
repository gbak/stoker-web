// ─── Device Types ───────────────────────────────────────────────

export type DeviceType = 'UNKNOWN' | 'PIT' | 'FOOD' | 'BLOWER';

export type AlarmType = 'NONE' | 'ALARM_FOOD' | 'ALARM_FIRE';

export interface SDevice {
  type: 'probe' | 'pitProbe' | 'fan';
  id: string;
  name: string;
  deviceType: DeviceType;
}

export interface StokerProbe extends SDevice {
  type: 'probe';
  deviceType: 'FOOD';
  targetTemp: number;
  lowerTempAlarm: number;
  upperTempAlarm: number;
  alarmType: AlarmType;
  currentTemp: number;
}

export interface StokerFan extends SDevice {
  type: 'fan';
  deviceType: 'BLOWER';
  fanOn: boolean;
  totalRuntime: number;
}

export interface StokerPitProbe extends SDevice {
  type: 'pitProbe';
  deviceType: 'PIT';
  targetTemp: number;
  lowerTempAlarm: number;
  upperTempAlarm: number;
  alarmType: AlarmType;
  currentTemp: number;
  fan: StokerFan | null;
}

// ─── Data Points ────────────────────────────────────────────────

export interface SDataPointBase {
  type: string;
  deviceId: string;
  deviceName: string | null;
  collectedTime: string; // ISO instant
  timedEvent: boolean;
}

export interface SProbeDataPoint extends SDataPointBase {
  type: 'probe';
  tempC: number;
  tempF: number;
}

export interface SBlowerDataPoint extends SDataPointBase {
  type: 'blower';
  fanOn: boolean;
  totalRuntime: number;
  blowerOnTime: string | null;
}

export type SDataPoint = SProbeDataPoint | SBlowerDataPoint;

// ─── Cooker / Configuration ─────────────────────────────────────

export interface Cooker {
  cookerName: string;
  pitSensor: StokerPitProbe | null;
  probes: StokerProbe[];
}

export interface CookerList {
  cookers: Cooker[];
  configRequired: boolean;
}

export interface ConfigurationSettings {
  cookerList: CookerList;
  availableDevices: SDevice[];
}

// ─── Events (WebSocket) ────────────────────────────────────────

export type ConnectionEventType =
  | 'NONE'
  | 'CONFIG_UPDATE'
  | 'CONFIG_UPDATE_REFRESH'
  | 'LOST_CONNECTION'
  | 'CONNECTION_ESTABLISHED'
  | 'EXTENDED_CONNECTION_LOSS';

export type HardwareStatus = 'UNKNOWN' | 'CONNECTED' | 'DISCONNECTED';

export interface HardwareDeviceState {
  status: HardwareStatus;
  timestamp: string;
}

export interface StateChangeEvent {
  eventType: ConnectionEventType;
  timestamp: string;
}

export interface DataPointEvent {
  dataPoints: SDataPoint[];
  timestamp: string;
}

// ─── Alerts ────────────────────────────────────────────────────

export interface AlertModel {
  type: 'connectionChange' | 'stokerAlarm' | 'tempAlarm' | 'timeAlert';
  name: string;
  enabled: boolean;
  availableDeliveryMethods: string[];
  configuredDeliveryMethods: string[];
}

export interface BrowserAlarm {
  alarmName: string;
  message: string;
  suppressDuration: string | null;
}

// ─── Logs ──────────────────────────────────────────────────────

export interface LogItem {
  logName: string;
  cookerName: string;
  startTime: string;
  devices: SDevice[];
  viewOnly?: boolean;
}

export interface LogFileEntry {
  fileName: string;
  filePath: string;
  relativePath: string;
  cookerName: string;
  logName: string;
  startTime: string;
  sizeBytes: number;
}

export interface LogNote {
  noteDate: string;
  note: string;
}

export interface LogDir {
  name: string;
  path: string;
  subdirectories: LogDir[];
  files: string[];
}

// ─── Weather ───────────────────────────────────────────────────

export interface WeatherData {
  city: string;
  state: string;
  currentTemperature: string;
  humidity: string;
  windSpeed: string;
  text: string;
  currentImage: string;
  code: string;
  forecastHigh: string;
  forecastLow: string;
  forecastCondition: string;
}

// ─── App-level state ───────────────────────────────────────────

export interface StokerState {
  connected: boolean;
  hardwareStatus: HardwareStatus;
  cookerList: CookerList | null;
  latestReadings: Map<string, SDataPoint>;
  temperatureHistory: Map<string, SProbeDataPoint[]>;
  weather: WeatherData | null;
  alarms: BrowserAlarm[];
}
