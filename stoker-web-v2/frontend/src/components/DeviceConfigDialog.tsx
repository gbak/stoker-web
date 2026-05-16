import { useState } from 'react';
import Modal from './Modal';
import { saveConfiguration } from '../api/stokerApi';
import type {
  ConfigurationSettings,
  SDevice,
  StokerProbe,
  StokerPitProbe,
  StokerFan,
  Cooker,
  CookerList,
  AlarmType,
} from '../types/stoker';

interface DeviceConfigDialogProps {
  settings: ConfigurationSettings;
  connected: boolean;
  onClose: () => void;
  onSaved: () => void;
}

interface DraftCooker {
  cookerName: string;
  pitId: string;
  fanId: string;
  probeIds: string[];
}

// Per-probe alarm settings kept separately so form state is flat
interface AlarmSettings {
  alarmType: AlarmType;
  targetTemp: number;
  lowerTempAlarm: number;
  upperTempAlarm: number;
}

function deviceLabel(d: SDevice): string {
  return d.name ? `${d.name} (${d.id.substring(0, 8)})` : d.id.substring(0, 8);
}

function collectAllDevices(settings: ConfigurationSettings): SDevice[] {
  const configured: SDevice[] = settings.cookerList?.cookers?.flatMap((c: Cooker) => {
    const devices: SDevice[] = [];
    if (c.pitSensor) { devices.push(c.pitSensor); if (c.pitSensor.fan) devices.push(c.pitSensor.fan); }
    devices.push(...c.probes);
    return devices;
  }) ?? [];
  return [...configured, ...settings.availableDevices];
}

function alarmLabel(t: AlarmType): string {
  if (t === 'ALARM_FOOD') return 'Food Alarm (target temp)';
  if (t === 'ALARM_FIRE') return 'Fire Alarm (temp range)';
  return 'None';
}

export default function DeviceConfigDialog({ settings, connected, onClose, onSaved }: DeviceConfigDialogProps) {
  const allDevices = collectAllDevices(settings);
  const probeDevices = allDevices.filter(d => d.type === 'probe' || d.type === 'pitProbe');
  const fanDevices   = allDevices.filter(d => d.type === 'fan');

  const [drafts, setDrafts] = useState<DraftCooker[]>(() =>
    settings.cookerList.cookers.map(c => ({
      cookerName: c.cookerName,
      pitId:    c.pitSensor?.id ?? '',
      fanId:    c.pitSensor?.fan?.id ?? '',
      probeIds: c.probes.map(p => p.id),
    }))
  );

  const [deviceNames, setDeviceNames] = useState<Record<string, string>>(() => {
    const m: Record<string, string> = {};
    allDevices.forEach(d => { m[d.id] = d.name; });
    return m;
  });

  // Alarm settings keyed by device id
  const [alarms, setAlarms] = useState<Record<string, AlarmSettings>>(() => {
    const m: Record<string, AlarmSettings> = {};
    allDevices.forEach(d => {
      if ('alarmType' in d) {
        const p = d as StokerProbe;
        m[d.id] = {
          alarmType:     p.alarmType     ?? 'NONE',
          targetTemp:    p.targetTemp    ?? 0,
          lowerTempAlarm: p.lowerTempAlarm ?? 0,
          upperTempAlarm: p.upperTempAlarm ?? 0,
        };
      }
    });
    return m;
  });

  const [saving, setSaving] = useState(false);
  const [error,  setError]  = useState<string | null>(null);

  // ── helpers ──────────────────────────────────────────────────

  const getAlarm = (id: string): AlarmSettings =>
    alarms[id] ?? { alarmType: 'NONE', targetTemp: 0, lowerTempAlarm: 0, upperTempAlarm: 0 };

  const patchAlarm = (id: string, patch: Partial<AlarmSettings>) =>
    setAlarms(prev => ({ ...prev, [id]: { ...getAlarm(id), ...patch } }));

  const addCooker = () =>
    setDrafts(prev => [...prev, { cookerName: `Cooker ${prev.length + 1}`, pitId: '', fanId: '', probeIds: [] }]);

  const removeCooker = (idx: number) =>
    setDrafts(prev => prev.filter((_, i) => i !== idx));

  const updateDraft = (idx: number, patch: Partial<DraftCooker>) =>
    setDrafts(prev => prev.map((d, i) => i === idx ? { ...d, ...patch } : d));

  const toggleProbe = (ci: number, id: string) =>
    setDrafts(prev => prev.map((d, i) => {
      if (i !== ci) return d;
      const ids = d.probeIds.includes(id) ? d.probeIds.filter(x => x !== id) : [...d.probeIds, id];
      return { ...d, probeIds: ids };
    }));

  // ── build payload ─────────────────────────────────────────────

  const buildCookerList = (): CookerList => {
    const cookers: Cooker[] = drafts
      .filter(d => d.cookerName.trim())
      .map(d => {
        const pitDevice = probeDevices.find(p => p.id === d.pitId);
        const fanDevice = fanDevices.find(f => f.id === d.fanId);
        const pitAlarm  = getAlarm(d.pitId);

        const fan: StokerFan | null = fanDevice
          ? { type: 'fan', id: fanDevice.id, name: deviceNames[fanDevice.id] ?? '', deviceType: 'BLOWER', fanOn: false, totalRuntime: 0 }
          : null;

        const pitSensor: StokerPitProbe | null = pitDevice
          ? {
              type: 'pitProbe', id: pitDevice.id,
              name: deviceNames[pitDevice.id] ?? '',
              deviceType: 'PIT',
              alarmType:      pitAlarm.alarmType,
              targetTemp:     pitAlarm.targetTemp,
              lowerTempAlarm: pitAlarm.lowerTempAlarm,
              upperTempAlarm: pitAlarm.upperTempAlarm,
              currentTemp: 0, fan,
            }
          : null;

        const probes: StokerProbe[] = d.probeIds
          .map(id => probeDevices.find(p => p.id === id))
          .filter(Boolean).map(p => p!)
          .map(p => {
            const a = getAlarm(p.id);
            return {
              type: 'probe' as const, id: p.id,
              name: deviceNames[p.id] ?? '',
              deviceType: 'FOOD' as const,
              alarmType:      a.alarmType,
              targetTemp:     a.targetTemp,
              lowerTempAlarm: a.lowerTempAlarm,
              upperTempAlarm: a.upperTempAlarm,
              currentTemp: 0,
            };
          });

        return { cookerName: d.cookerName.trim(), pitSensor, probes };
      });

    return { cookers, configRequired: false };
  };

  const handleSave = async () => {
    setSaving(true); setError(null);
    try {
      await saveConfiguration(buildCookerList());
      onSaved();
      onClose();
    } catch (e: any) {
      setError(e.message || 'Failed to save configuration.');
    } finally {
      setSaving(false);
    }
  };

  // ── derived sets ──────────────────────────────────────────────

  const assignedPitIds   = new Set(drafts.map(d => d.pitId).filter(Boolean));
  const assignedFanIds   = new Set(drafts.map(d => d.fanId).filter(Boolean));
  const assignedProbeIds = new Set(drafts.flatMap(d => d.probeIds));

  // ── render ────────────────────────────────────────────────────

  return (
    <Modal title="Device Configuration" onClose={onClose} width="max-w-3xl">
      <div className="p-5 space-y-5">
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 rounded px-3 py-2 text-sm">{error}</div>
        )}

        {!connected && (
          <div className="bg-yellow-900/30 border border-yellow-700/60 text-yellow-300 rounded-lg px-3 py-2 text-sm flex items-center gap-2">
            <svg className="w-4 h-4 shrink-0 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v3m0 12v3M4.22 4.22l2.12 2.12m11.32 11.32 2.12 2.12M3 12h3m12 0h3M4.22 19.78l2.12-2.12M17.66 6.34l2.12-2.12" />
            </svg>
            Not connected to Stoker — device discovery pending. Available sensors may be incomplete until the device connects.
          </div>
        )}

        {connected && settings.availableDevices.length > 0 && (
          <div className="bg-stoker-accent/20 border border-gray-700 rounded-lg p-3">
            <p className="text-xs text-gray-400 font-semibold mb-1">Unassigned devices detected:</p>
            <div className="flex flex-wrap gap-2">
              {settings.availableDevices.map(d => (
                <span key={d.id} className="text-xs bg-gray-700 text-gray-300 px-2 py-1 rounded">
                  {d.id.substring(0, 8)} ({d.type})
                </span>
              ))}
            </div>
          </div>
        )}

        <div className="space-y-4">
          {drafts.map((draft, ci) => {
            const availablePits   = probeDevices.filter(p => p.id === draft.pitId || (!assignedPitIds.has(p.id) && !assignedProbeIds.has(p.id)));
            const availableFans   = fanDevices.filter(f => f.id === draft.fanId || !assignedFanIds.has(f.id));
            const availableProbes = probeDevices.filter(p => draft.probeIds.includes(p.id) || (!assignedPitIds.has(p.id) && !assignedProbeIds.has(p.id)));
            const pitAlarm = getAlarm(draft.pitId);

            return (
              <div key={ci} className="border border-gray-700 rounded-xl p-4 space-y-4">
                {/* Cooker name row */}
                <div className="flex items-center justify-between">
                  <input
                    type="text"
                    value={draft.cookerName}
                    onChange={e => updateDraft(ci, { cookerName: e.target.value })}
                    placeholder="Cooker name"
                    className="bg-stoker-dark border border-gray-600 rounded px-3 py-1.5 text-gray-100 text-sm font-medium focus:border-stoker-orange focus:outline-none w-48"
                  />
                  <button onClick={() => removeCooker(ci)} className="text-xs text-gray-500 hover:text-red-400">
                    Remove cooker
                  </button>
                </div>

                {/* Pit probe + fan selectors */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs text-gray-400 mb-1">Pit Probe</label>
                    <select
                      value={draft.pitId}
                      onChange={e => updateDraft(ci, { pitId: e.target.value })}
                      className="w-full bg-stoker-dark border border-gray-600 rounded px-2 py-1.5 text-gray-100 text-sm"
                    >
                      <option value="">-- none --</option>
                      {availablePits.map(p => <option key={p.id} value={p.id}>{deviceLabel(p)}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-400 mb-1">Blower Fan</label>
                    <select
                      value={draft.fanId}
                      onChange={e => updateDraft(ci, { fanId: e.target.value })}
                      disabled={!draft.pitId}
                      className="w-full bg-stoker-dark border border-gray-600 rounded px-2 py-1.5 text-gray-100 text-sm disabled:opacity-50"
                    >
                      <option value="">-- none --</option>
                      {availableFans.map(f => <option key={f.id} value={f.id}>{deviceLabel(f)}</option>)}
                    </select>
                  </div>
                </div>

                {/* Pit probe alarm config */}
                {draft.pitId && (
                  <div className="bg-stoker-dark/50 rounded-lg p-3 space-y-2">
                    <div className="flex items-center gap-3 flex-wrap">
                      <label className="text-xs text-gray-400 w-20 shrink-0">Pit name</label>
                      <input
                        type="text"
                        value={deviceNames[draft.pitId] ?? ''}
                        onChange={e => setDeviceNames(prev => ({ ...prev, [draft.pitId]: e.target.value }))}
                        placeholder="e.g. Pit"
                        className="flex-1 min-w-28 bg-stoker-dark border border-gray-600 rounded px-2 py-1 text-gray-100 text-sm"
                      />
                      <label className="text-xs text-gray-400 w-20 shrink-0">Alarm type</label>
                      <select
                        value={pitAlarm.alarmType}
                        onChange={e => patchAlarm(draft.pitId, { alarmType: e.target.value as AlarmType })}
                        className="flex-1 min-w-36 bg-stoker-dark border border-gray-600 rounded px-2 py-1 text-gray-100 text-sm"
                      >
                        {(['NONE', 'ALARM_FOOD', 'ALARM_FIRE'] as AlarmType[]).map(t => (
                          <option key={t} value={t}>{alarmLabel(t)}</option>
                        ))}
                      </select>
                    </div>
                    <AlarmFields id={draft.pitId} alarm={pitAlarm} onChange={patchAlarm} />
                  </div>
                )}

                {/* Food probes */}
                <div>
                  <label className="block text-xs text-gray-400 mb-2">Food Probes</label>
                  {availableProbes.length === 0 ? (
                    <p className="text-xs text-gray-500">No probe devices available.</p>
                  ) : (
                    <div className="space-y-3">
                      {availableProbes.map(probe => {
                        const selected = draft.probeIds.includes(probe.id);
                        const pa = getAlarm(probe.id);
                        return (
                          <div key={probe.id} className={`rounded-lg border transition-colors ${selected ? 'border-gray-600 bg-stoker-dark/40' : 'border-transparent'}`}>
                            {/* Header row */}
                            <div className="flex items-center gap-3 px-3 py-2">
                              <input
                                type="checkbox"
                                checked={selected}
                                onChange={() => toggleProbe(ci, probe.id)}
                                className="accent-stoker-orange shrink-0"
                              />
                              <span className="text-xs text-gray-400 w-20 shrink-0 font-mono">{probe.id.substring(0, 8)}</span>
                              {selected && (
                                <input
                                  type="text"
                                  value={deviceNames[probe.id] ?? ''}
                                  onChange={e => setDeviceNames(prev => ({ ...prev, [probe.id]: e.target.value }))}
                                  placeholder="Name (e.g. Brisket)"
                                  className="flex-1 bg-stoker-dark border border-gray-600 rounded px-2 py-1 text-gray-100 text-xs"
                                />
                              )}
                            </div>
                            {/* Alarm config for selected probes */}
                            {selected && (
                              <div className="px-3 pb-3 space-y-2">
                                <div className="flex items-center gap-3">
                                  <label className="text-xs text-gray-400 w-20 shrink-0">Alarm type</label>
                                  <select
                                    value={pa.alarmType}
                                    onChange={e => patchAlarm(probe.id, { alarmType: e.target.value as AlarmType })}
                                    className="flex-1 bg-stoker-dark border border-gray-600 rounded px-2 py-1 text-gray-100 text-xs"
                                  >
                                    {(['NONE', 'ALARM_FOOD', 'ALARM_FIRE'] as AlarmType[]).map(t => (
                                      <option key={t} value={t}>{alarmLabel(t)}</option>
                                    ))}
                                  </select>
                                </div>
                                <AlarmFields id={probe.id} alarm={pa} onChange={patchAlarm} small />
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <button
          onClick={addCooker}
          className="text-sm text-stoker-orange hover:text-orange-400 border border-dashed border-stoker-orange/40 hover:border-stoker-orange/70 rounded-lg px-4 py-2 w-full"
        >
          + Add Cooker
        </button>

        <div className="flex justify-end gap-3 pt-2">
          <button onClick={onClose} className="px-4 py-2 text-sm text-gray-400 hover:text-gray-200">Cancel</button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-5 py-2 bg-stoker-orange text-white text-sm font-medium rounded hover:bg-orange-500 disabled:opacity-50"
          >
            {saving ? 'Saving...' : 'Save Configuration'}
          </button>
        </div>
      </div>
    </Modal>
  );
}

// ── Alarm temp fields — reused for both pit and food probes ──────

interface AlarmFieldsProps {
  id: string;
  alarm: AlarmSettings;
  onChange: (id: string, patch: Partial<AlarmSettings>) => void;
  small?: boolean;
}

function AlarmFields({ id, alarm, onChange, small }: AlarmFieldsProps) {
  const cls = `bg-stoker-dark border border-gray-600 rounded px-2 py-1 text-gray-100 ${small ? 'text-xs' : 'text-sm'} w-24`;

  if (alarm.alarmType === 'ALARM_FOOD') {
    return (
      <div className="flex items-center gap-3">
        <label className={`text-xs text-gray-400 w-20 shrink-0`}>Target (°F)</label>
        <input
          type="number" min={0} max={500} step={5}
          value={alarm.targetTemp || ''}
          onChange={e => onChange(id, { targetTemp: Number(e.target.value) })}
          placeholder="e.g. 205"
          className={cls}
        />
      </div>
    );
  }

  if (alarm.alarmType === 'ALARM_FIRE') {
    return (
      <div className="flex items-center gap-3 flex-wrap">
        <label className={`text-xs text-gray-400 w-20 shrink-0`}>Low (°F)</label>
        <input
          type="number" min={0} max={500} step={5}
          value={alarm.lowerTempAlarm || ''}
          onChange={e => onChange(id, { lowerTempAlarm: Number(e.target.value) })}
          placeholder="e.g. 225"
          className={cls}
        />
        <label className={`text-xs text-gray-400 shrink-0`}>High (°F)</label>
        <input
          type="number" min={0} max={500} step={5}
          value={alarm.upperTempAlarm || ''}
          onChange={e => onChange(id, { upperTempAlarm: Number(e.target.value) })}
          placeholder="e.g. 275"
          className={cls}
        />
      </div>
    );
  }

  return null;
}
