import { useState, useEffect } from 'react';
import Modal from './Modal';
import { getAlertConfig, saveAlertConfig, getDeliveryMethods } from '../api/stokerApi';
import type { AlertModel } from '../types/stoker';

interface AlertsConfigDialogProps {
  onClose: () => void;
}

export default function AlertsConfigDialog({ onClose }: AlertsConfigDialogProps) {
  const [alerts, setAlerts] = useState<AlertModel[]>([]);
  const [deliveryMethods, setDeliveryMethods] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    Promise.all([getAlertConfig(), getDeliveryMethods()])
      .then(([cfg, methods]) => {
        setAlerts(cfg);
        setDeliveryMethods(methods);
      })
      .catch(() => setError('Failed to load alert configuration.'))
      .finally(() => setLoading(false));
  }, []);

  const toggleEnabled = (idx: number) => {
    setAlerts(prev => prev.map((a, i) =>
      i === idx ? { ...a, enabled: !a.enabled } : a
    ));
  };

  const toggleDelivery = (alertIdx: number, method: string) => {
    setAlerts(prev => prev.map((a, i) => {
      if (i !== alertIdx) return a;
      const methods = new Set(a.configuredDeliveryMethods);
      if (methods.has(method)) methods.delete(method); else methods.add(method);
      return { ...a, configuredDeliveryMethods: [...methods] };
    }));
  };

  const handleSave = async () => {
    setSaving(true); setError(null);
    try {
      await saveAlertConfig(alerts);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } catch {
      setError('Failed to save alert configuration.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title="Alert Configuration" onClose={onClose}>
      <div className="p-5 space-y-4">
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 rounded px-3 py-2 text-sm">
            {error}
          </div>
        )}

        {loading ? (
          <p className="text-gray-400 text-sm">Loading...</p>
        ) : (
          <>
            <div className="space-y-3">
              {alerts.map((alert, i) => (
                <div key={alert.name} className="border border-gray-700 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <span className="font-medium text-gray-200">{alert.name}</span>
                    <label className="flex items-center gap-2 cursor-pointer select-none">
                      <span className="text-xs text-gray-400">{alert.enabled ? 'Enabled' : 'Disabled'}</span>
                      <div
                        onClick={() => toggleEnabled(i)}
                        className={`w-10 h-5 rounded-full transition-colors ${
                          alert.enabled ? 'bg-stoker-orange' : 'bg-gray-600'
                        } relative cursor-pointer`}
                      >
                        <div
                          className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform ${
                            alert.enabled ? 'translate-x-5' : 'translate-x-0.5'
                          }`}
                        />
                      </div>
                    </label>
                  </div>

                  {alert.enabled && deliveryMethods.length > 0 && (
                    <div>
                      <p className="text-xs text-gray-400 mb-2">Delivery methods:</p>
                      <div className="flex flex-wrap gap-2">
                        {deliveryMethods.map(method => {
                          const active = alert.configuredDeliveryMethods.includes(method);
                          return (
                            <button
                              key={method}
                              onClick={() => toggleDelivery(i, method)}
                              className={`px-3 py-1 text-xs rounded border transition-colors ${
                                active
                                  ? 'border-stoker-orange bg-stoker-orange/20 text-stoker-orange'
                                  : 'border-gray-600 text-gray-400 hover:border-gray-400'
                              }`}
                            >
                              {method}
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <button
                onClick={onClose}
                className="px-4 py-2 text-sm text-gray-400 hover:text-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                disabled={saving}
                className="px-5 py-2 bg-stoker-orange text-white text-sm font-medium rounded hover:bg-orange-500 disabled:opacity-50"
              >
                {saving ? 'Saving...' : saved ? 'Saved!' : 'Save'}
              </button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
