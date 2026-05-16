import ReactECharts from 'echarts-for-react';
import type { SBlowerDataPoint, SProbeDataPoint, StokerProbe, StokerPitProbe, AlarmType } from '../types/stoker';

interface ProbeGaugeProps {
  device: StokerProbe | StokerPitProbe;
  reading: SProbeDataPoint | null;
  fanReading?: SBlowerDataPoint | null;
}

export default function ProbeGauge({ device, reading, fanReading }: ProbeGaugeProps) {
  const temp = reading?.tempF ?? 0;
  const { alarmType, targetTemp, lowerTempAlarm, upperTempAlarm } = device;

  const maxTemp = computeMax(alarmType, targetTemp, upperTempAlarm);
  const axisColors = buildAxisColors(alarmType, targetTemp, lowerTempAlarm, upperTempAlarm, maxTemp);

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 220,
        endAngle: -40,
        min: 0,
        max: maxTemp,
        radius: '100%',
        progress: {
          show: true,
          width: 10,
          itemStyle: { color: temp > 0 ? currentProgressColor(temp, alarmType, targetTemp, lowerTempAlarm, upperTempAlarm) : '#555' },
        },
        axisLine: {
          lineStyle: { width: 14, color: axisColors },
        },
        axisTick: { show: false },
        splitLine: {
          length: 8,
          lineStyle: { width: 2, color: '#555' },
        },
        axisLabel: {
          distance: 20,
          color: '#888',
          fontSize: 10,
        },
        pointer: {
          length: '55%',
          width: 4,
          itemStyle: { color: '#ff6b35' },
        },
        anchor: {
          show: true,
          size: 8,
          itemStyle: { borderColor: '#ff6b35', borderWidth: 2 },
        },
        title: { show: false },
        detail: {
          valueAnimation: true,
          fontSize: 20,
          fontWeight: 'bold',
          offsetCenter: [0, '55%'],
          formatter: (v: number) => v > 0 ? `${v.toFixed(1)}°F` : '--',
          color: '#eee',
        },
        data: [{ value: temp, name: '' }],
      },
    ],
  };

  const name = device.name || device.id.substring(0, 8);
  const fan = device.type === 'pitProbe' ? (device as StokerPitProbe).fan : null;
  const fanOn = fanReading?.fanOn ?? fan?.fanOn ?? false;

  return (
    <div className="bg-stoker-accent/30 rounded-lg p-2 inline-block">
      <div className="text-center text-xl font-bold text-gray-100 truncate px-1 pt-1">
        {name}
      </div>
      <ReactECharts
        option={option}
        style={{ height: 200, width: 200 }}
        opts={{ renderer: 'canvas' }}
      />
      <div className="text-center text-xs text-gray-500 -mt-2 h-4">
        {alarmType === 'ALARM_FOOD' && targetTemp > 0 && (
          <span>Target: {targetTemp}°F</span>
        )}
        {alarmType === 'ALARM_FIRE' && (lowerTempAlarm > 0 || upperTempAlarm > 0) && (
          <span>{lowerTempAlarm}–{upperTempAlarm}°F</span>
        )}
      </div>
      {fan && (
        <div className="flex items-center justify-center gap-2 mt-1 pb-1">
          <div className={`w-3 h-3 rounded-full ${fanOn ? 'bg-green-400 animate-pulse' : 'bg-gray-600'}`} />
          <span className={`text-xs font-medium ${fanOn ? 'text-green-400' : 'text-gray-500'}`}>
            {fan.name || 'Blower'} {fanOn ? 'ON' : 'OFF'}
          </span>
        </div>
      )}
    </div>
  );
}

/** Max scale: leave headroom above the highest alarm point */
function computeMax(alarmType: AlarmType, targetTemp: number, upperTempAlarm: number): number {
  if (alarmType === 'ALARM_FOOD' && targetTemp > 0) {
    return Math.max(Math.ceil((targetTemp + 50) / 50) * 50, 200);
  }
  if (alarmType === 'ALARM_FIRE' && upperTempAlarm > 0) {
    return Math.max(Math.ceil((upperTempAlarm + 75) / 50) * 50, 300);
  }
  return 500;
}

const SLATE  = '#334155'; // no-color zone
const ORANGE = '#eab308'; // alarm zone (yellow)

/**
 * Arc color zones:
 *  ALARM_FOOD  → slate below target, orange at/above target
 *  ALARM_FIRE  → orange below lower, slate in good range, orange above upper
 *  NONE        → uniform slate
 */
function buildAxisColors(
  alarmType: AlarmType,
  targetTemp: number,
  lowerTempAlarm: number,
  upperTempAlarm: number,
  maxTemp: number
): [number, string][] {
  if (alarmType === 'ALARM_FOOD' && targetTemp > 0) {
    const atTarget = Math.min(targetTemp / maxTemp, 0.99);
    return [
      [atTarget, SLATE],  // no color below target
      [1,        ORANGE], // orange at/above target
    ];
  }

  if (alarmType === 'ALARM_FIRE') {
    const colors: [number, string][] = [];
    if (lowerTempAlarm > 0) {
      colors.push([lowerTempAlarm / maxTemp, ORANGE]); // orange: too cold
    }
    const upperFrac = upperTempAlarm > 0
      ? Math.min(upperTempAlarm / maxTemp, 0.99)
      : 0.85;
    colors.push([upperFrac, SLATE]);  // no color: good range
    colors.push([1,         ORANGE]); // orange: too hot
    return colors;
  }

  return [[1, SLATE]]; // NONE: uniform slate
}

/** Progress bar color: orange when in an alarm zone, slate otherwise */
function currentProgressColor(
  temp: number,
  alarmType: AlarmType,
  targetTemp: number,
  lowerTempAlarm: number,
  upperTempAlarm: number
): string {
  if (alarmType === 'ALARM_FOOD' && targetTemp > 0) {
    return temp >= targetTemp ? ORANGE : SLATE;
  }
  if (alarmType === 'ALARM_FIRE') {
    const tooHot  = upperTempAlarm > 0 && temp > upperTempAlarm;
    const tooCold = lowerTempAlarm > 0 && temp < lowerTempAlarm;
    return (tooHot || tooCold) ? ORANGE : SLATE;
  }
  return ORANGE; // NONE: always yellow
}
