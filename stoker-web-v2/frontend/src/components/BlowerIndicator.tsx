import type { SBlowerDataPoint, StokerFan } from '../types/stoker';

interface BlowerIndicatorProps {
  fan: StokerFan;
  reading: SBlowerDataPoint | null;
}

export default function BlowerIndicator({ fan, reading }: BlowerIndicatorProps) {
  const isOn = reading?.fanOn ?? fan.fanOn;
  const runtime = reading?.totalRuntime ?? fan.totalRuntime;

  return (
    <div className="bg-stoker-accent/30 rounded-lg px-4 py-3 flex items-center gap-3">
      <div
        className={`w-4 h-4 rounded-full ${
          isOn ? 'bg-green-400 animate-pulse' : 'bg-gray-600'
        }`}
      />
      <div>
        <div className="text-sm font-medium">
          {fan.name || 'Blower'}{' '}
          <span className={isOn ? 'text-green-400' : 'text-gray-500'}>
            {isOn ? 'ON' : 'OFF'}
          </span>
        </div>
        {runtime > 0 && (
          <div className="text-xs text-gray-400">
            Runtime: {formatRuntime(runtime)}
          </div>
        )}
      </div>
    </div>
  );
}

function formatRuntime(ms: number): string {
  const totalSec = Math.floor(ms / 1000);
  const hours = Math.floor(totalSec / 3600);
  const minutes = Math.floor((totalSec % 3600) / 60);
  const seconds = totalSec % 60;
  if (hours > 0) return `${hours}h ${minutes}m`;
  if (minutes > 0) return `${minutes}m ${seconds}s`;
  return `${seconds}s`;
}
