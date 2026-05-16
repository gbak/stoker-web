import type {
  ConfigurationSettings,
  CookerList,
  LogItem,
  LogNote,
  LogDir,
  LogFileEntry,
  AlertModel,
  WeatherData,
} from '../types/stoker';

const BASE = '/api/v1';

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) {
    const text = await res.text();
    try {
      const body = JSON.parse(text);
      if (body.error) throw new Error(body.error);
    } catch (e) {
      if (e instanceof SyntaxError) throw new Error(`${res.status} ${res.statusText}`);
      throw e;
    }
    throw new Error(`${res.status} ${res.statusText}`);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : (undefined as unknown as T);
}

// ─── Devices ───────────────────────────────────────────────────

export function getStatus() {
  return fetchJson<{ hardwareStatus: string; connectedSince: string | null }>(`${BASE}/status`);
}

export function getConfiguration() {
  return fetchJson<ConfigurationSettings>(`${BASE}/configuration`);
}

export function saveConfiguration(settings: Pick<ConfigurationSettings, 'cookerList'> | CookerList) {
  const body = 'cookers' in settings ? settings : settings.cookerList;
  return fetchJson<void>(`${BASE}/configuration`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

// ─── Logs ──────────────────────────────────────────────────────

export function getLogs() {
  return fetchJson<LogDir>(`${BASE}/logs`);
}

export function getActiveLogs() {
  return fetchJson<LogItem[]>(`${BASE}/logs`);
}

export function startLog(cookerName: string, logName: string) {
  return fetchJson<{ status: string; logName: string }>(`${BASE}/logs/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ cookerName, logName }),
  });
}

export function stopLog(logName: string) {
  return fetchJson<{ status: string; fileName: string }>(`${BASE}/logs/stop`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ logName }),
  });
}

export function getLogData(logName: string) {
  return fetchJson<unknown[]>(`${BASE}/logs/${encodeURIComponent(logName)}/data`);
}

export function getLogNotes(logName: string) {
  return fetchJson<LogNote[]>(`${BASE}/logs/${encodeURIComponent(logName)}/notes`);
}

export function addLogNote(logName: string, note: string) {
  return fetchJson<void>(`${BASE}/logs/${encodeURIComponent(logName)}/notes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ note }),
  });
}

export function getLogFiles() {
  return fetchJson<LogFileEntry[]>(`${BASE}/logs/files`);
}

export function loadLog(filePath: string, resume: boolean) {
  return fetchJson<LogItem>(`${BASE}/logs/load`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ filePath, resume }),
  });
}

export function closeViewLog(logName: string) {
  return fetchJson<void>(`${BASE}/logs/close`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ logName }),
  });
}

// ─── Alerts ────────────────────────────────────────────────────

export function getAlertConfig() {
  return fetchJson<AlertModel[]>(`${BASE}/alerts/configuration`);
}

export function saveAlertConfig(alerts: AlertModel[]) {
  return fetchJson<void>(`${BASE}/alerts/configuration`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(alerts),
  });
}

export function getDeliveryMethods() {
  return fetchJson<string[]>(`${BASE}/alerts/delivery-methods`);
}

// ─── Weather ───────────────────────────────────────────────────

export function getWeather() {
  return fetchJson<WeatherData>(`${BASE}/weather`);
}

// ─── Auth ──────────────────────────────────────────────────────

export async function login(username: string, password: string): Promise<boolean> {
  const body = new URLSearchParams({ username, password });
  const res = await fetch('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
    redirect: 'manual', // Spring returns a redirect on success
  });
  // 200 or 3xx redirect both mean success; 401 means bad credentials
  return res.ok || res.type === 'opaqueredirect';
}

export async function logout(): Promise<void> {
  await fetch('/api/logout', { method: 'POST' });
}

export async function checkAuth(): Promise<boolean> {
  const res = await fetch(`${BASE}/alerts/configuration`, { method: 'HEAD' });
  return res.ok;
}
