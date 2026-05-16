import type { BrowserAlarm } from '../types/stoker';

interface AlarmBannerProps {
  alarms: BrowserAlarm[];
  onDismiss: (index: number) => void;
}

export default function AlarmBanner({ alarms, onDismiss }: AlarmBannerProps) {
  if (alarms.length === 0) return null;

  return (
    <div className="space-y-1 px-4 pt-2">
      {alarms.map((alarm, i) => (
        <div
          key={i}
          className="bg-red-900/70 border border-red-500 text-red-100 rounded px-4 py-2 flex items-center justify-between animate-pulse"
        >
          <div>
            <span className="font-semibold">{alarm.alarmName}:</span>{' '}
            {alarm.message}
          </div>
          <button
            onClick={() => onDismiss(i)}
            className="ml-4 text-red-300 hover:text-white text-lg leading-none"
          >
            &times;
          </button>
        </div>
      ))}
    </div>
  );
}
