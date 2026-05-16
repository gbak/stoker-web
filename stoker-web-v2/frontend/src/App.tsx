import { useEffect, useState, useCallback } from 'react';
import { useStokerWebSocket } from './hooks/useStokerWebSocket';
import { getConfiguration, checkAuth, logout } from './api/stokerApi';
import Header from './components/Header';
import CookerPanel from './components/CookerPanel';
import AlarmBanner from './components/AlarmBanner';
import LoginDialog from './components/LoginDialog';
import LogManagementDialog from './components/LogManagementDialog';
import AlertsConfigDialog from './components/AlertsConfigDialog';
import DeviceConfigDialog from './components/DeviceConfigDialog';
import type { ConfigurationSettings } from './types/stoker';

type Dialog = 'login' | 'logs' | 'alerts' | 'config' | null;

export default function App() {
  const ws = useStokerWebSocket();
  const [settings, setSettings] = useState<ConfigurationSettings | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [dialog, setDialog] = useState<Dialog>(null);

  const loadConfig = useCallback(() => {
    getConfiguration()
      .then(setSettings)
      .catch(err => setError(err.message));
  }, []);

  useEffect(() => {
    loadConfig();
    checkAuth().then(setAuthenticated);
  }, [loadConfig]);

  // Refresh config on CONFIG_UPDATE events or when the device connects
  // (connection triggers hardware discovery which populates availableDevices)
  useEffect(() => {
    const lastEvent = ws.events[ws.events.length - 1];
    if (
      lastEvent &&
      (lastEvent.eventType === 'CONFIG_UPDATE' ||
        lastEvent.eventType === 'CONFIG_UPDATE_REFRESH' ||
        lastEvent.eventType === 'CONNECTION_ESTABLISHED')
    ) {
      loadConfig();
    }
  }, [ws.events, loadConfig]);

  const handleLogout = async () => {
    await logout();
    setAuthenticated(false);
  };

  const cookers = settings?.cookerList?.cookers ?? [];
  const cookerNames = cookers.map(c => c.cookerName);

  return (
    <div className="min-h-screen flex flex-col">
      <Header
        connected={ws.connected}
        authenticated={authenticated}
        onLogin={() => setDialog('login')}
        onLogout={handleLogout}
        onLogs={() => setDialog('logs')}
        onAlerts={() => setDialog('alerts')}
        onConfig={() => setDialog('config')}
      />

      <AlarmBanner alarms={ws.alarms} onDismiss={ws.dismissAlarm} />

      <main className="flex-1 p-4 max-w-7xl mx-auto w-full">
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 rounded p-3 mb-4 text-sm">
            Failed to load configuration: {error}
          </div>
        )}

        {cookers.length === 0 && !error && (
          <div className="text-center text-gray-400 mt-12">
            {settings?.cookerList?.configRequired
              ? 'Device configuration required. Sign in and use Configure to assign your probes.'
              : 'Waiting for device data...'}
          </div>
        )}

        <div className="space-y-6">
          {cookers.map(cooker => (
            <CookerPanel
              key={cooker.cookerName}
              cooker={cooker}
              readings={ws.latestReadings}
              temperatureHistory={ws.temperatureHistory}
            />
          ))}
        </div>
      </main>

      <footer className="text-center text-gray-500 text-xs py-3 border-t border-gray-700">
        Stoker-Web &middot; Real-time BBQ Monitor
      </footer>

      {/* Dialogs */}
      {dialog === 'login' && (
        <LoginDialog
          onClose={() => setDialog(null)}
          onLoginSuccess={() => setAuthenticated(true)}
        />
      )}
      {dialog === 'logs' && (
        <LogManagementDialog
          cookerNames={cookerNames}
          onClose={() => setDialog(null)}
        />
      )}
      {dialog === 'alerts' && (
        <AlertsConfigDialog onClose={() => setDialog(null)} />
      )}
      {dialog === 'config' && settings && (
        <DeviceConfigDialog
          key={settings.availableDevices.map(d => d.id).join(',')}
          settings={settings}
          connected={ws.connected}
          onClose={() => setDialog(null)}
          onSaved={loadConfig}
        />
      )}
    </div>
  );
}
