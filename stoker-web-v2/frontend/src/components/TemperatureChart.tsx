import ReactECharts from 'echarts-for-react';
import type { SProbeDataPoint, SDataPoint } from '../types/stoker';

interface TemperatureChartProps {
  history: Record<string, SProbeDataPoint[]>;
  readings: Record<string, SDataPoint>;
}

const COLORS = [
  '#ff6b35', '#3b82f6', '#22c55e', '#f59e0b',
  '#a855f7', '#ec4899', '#14b8a6', '#f97316',
];

export default function TemperatureChart({ history, readings }: TemperatureChartProps) {
  const deviceIds = Object.keys(history);
  if (deviceIds.length === 0) return null;

  const series = deviceIds.map((id, i) => {
    const points = history[id];
    const reading = readings[id];
    const name = reading?.deviceName || id.substring(0, 8);

    return {
      name,
      type: 'line' as const,
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 2 },
      color: COLORS[i % COLORS.length],
      data: points.map(p => [p.collectedTime, p.tempF]),
    };
  });

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1a1a2e',
      borderColor: '#333',
      textStyle: { color: '#eee' },
      formatter: (params: any[]) => {
        if (!params.length) return '';
        const time = new Date(params[0].value[0]).toLocaleTimeString();
        const lines = params.map(
          (p: any) => `<span style="color:${p.color}">\u25CF</span> ${p.seriesName}: ${p.value[1].toFixed(1)}°F`
        );
        return `${time}<br/>${lines.join('<br/>')}`;
      },
    },
    legend: {
      data: series.map(s => s.name),
      textStyle: { color: '#999' },
      top: 0,
    },
    grid: {
      left: 50,
      right: 20,
      top: 30,
      bottom: 30,
    },
    xAxis: {
      type: 'time',
      axisLine: { lineStyle: { color: '#555' } },
      axisLabel: {
        color: '#999',
        formatter: (val: number) => new Date(val).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      name: '°F',
      nameTextStyle: { color: '#999' },
      axisLine: { lineStyle: { color: '#555' } },
      axisLabel: { color: '#999' },
      splitLine: { lineStyle: { color: '#333' } },
    },
    series,
  };

  return (
    <div className="bg-stoker-panel rounded-xl border border-gray-700 p-4">
      <h2 className="text-lg font-semibold text-gray-300 mb-2">Temperature History</h2>
      <ReactECharts
        option={option}
        style={{ height: 350 }}
        opts={{ renderer: 'canvas' }}
      />
    </div>
  );
}
