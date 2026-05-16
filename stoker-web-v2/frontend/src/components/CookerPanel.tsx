import { useState } from 'react';
import type { Cooker, SDataPoint, SProbeDataPoint, SBlowerDataPoint, StokerProbe, StokerPitProbe } from '../types/stoker';
import ProbeGauge from './ProbeGauge';
import TemperatureChart from './TemperatureChart';

interface CookerPanelProps {
  cooker: Cooker;
  readings: Record<string, SDataPoint>;
  temperatureHistory: Record<string, SProbeDataPoint[]>;
}

export default function CookerPanel({ cooker, readings, temperatureHistory }: CookerPanelProps) {
  const { pitSensor, probes, cookerName } = cooker;
  const [chartExpanded, setChartExpanded] = useState(false);

  const cookerDeviceIds = new Set([
    ...(pitSensor ? [pitSensor.id] : []),
    ...probes.map(p => p.id),
  ]);
  const cookerHistory = Object.fromEntries(
    Object.entries(temperatureHistory).filter(([id]) => cookerDeviceIds.has(id))
  );
  const hasHistory = Object.keys(cookerHistory).length > 0;

  const probeDevices: (StokerPitProbe | StokerProbe)[] = [
    ...(pitSensor ? [pitSensor] : []),
    ...probes,
  ];

  return (
    <div className="bg-stoker-panel rounded-xl border border-gray-700 p-4">
      <h2 className="text-lg font-semibold text-stoker-orange mb-3">
        {cookerName}
      </h2>

      <div className="flex gap-0 items-stretch">

        {/* Left column: gauges (full) or text summary (when chart expanded) */}
        <div className={`shrink-0 transition-all duration-300 ${chartExpanded ? 'w-28' : ''}`}>
          {chartExpanded ? (
            // Compact text summary
            <div className="flex flex-col gap-3 pr-2">
              {probeDevices.map(device => {
                const reading = readings[device.id] as SProbeDataPoint | undefined;
                return (
                  <div key={device.id} className="text-center">
                    <div className="text-xs text-gray-400 truncate">
                      {device.name || device.id.substring(0, 8)}
                    </div>
                    <div className="text-lg font-bold text-gray-100 leading-tight">
                      {reading ? `${reading.tempF.toFixed(1)}°F` : '--'}
                    </div>
                    {device.targetTemp > 0 && (
                      <div className="text-xs text-gray-500">
                        /{device.targetTemp}°F
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          ) : (
            // Full gauges
            <div className="flex flex-wrap gap-4 items-start">
              {pitSensor && (
                <ProbeGauge
                  device={pitSensor}
                  reading={readings[pitSensor.id] as SProbeDataPoint | undefined ?? null}
                  fanReading={pitSensor.fan ? readings[pitSensor.fan.id] as SBlowerDataPoint | undefined ?? null : null}
                />
              )}
              {probes.map(probe => (
                <ProbeGauge
                  key={probe.id}
                  device={probe}
                  reading={readings[probe.id] as SProbeDataPoint | undefined ?? null}
                />
              ))}
            </div>
          )}
        </div>

        {/* Chevron divider — only shown when chart is available */}
        {hasHistory && (
          <div className="flex items-center px-1">
            <button
              onClick={() => setChartExpanded(e => !e)}
              className="flex items-center justify-center w-5 self-stretch text-gray-500 hover:text-stoker-orange transition-colors"
              title={chartExpanded ? 'Collapse chart' : 'Expand chart'}
            >
              <span className="text-lg select-none">
                {chartExpanded ? '›' : '‹'}
              </span>
            </button>
          </div>
        )}

        {/* Right column: temperature chart */}
        {hasHistory && (
          <div className="flex-1 min-w-0">
            <TemperatureChart
              history={cookerHistory}
              readings={readings}
            />
          </div>
        )}

      </div>
    </div>
  );
}
