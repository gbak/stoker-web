import { useEffect, useRef, useCallback, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type {
  SDataPoint,
  SProbeDataPoint,
  StateChangeEvent,
  BrowserAlarm,
  WeatherData,
} from '../types/stoker';

const MAX_HISTORY = 300; // ~5 hours at 1/min

export interface StokerWebSocketState {
  connected: boolean;
  latestReadings: Record<string, SDataPoint>;
  temperatureHistory: Record<string, SProbeDataPoint[]>;
  events: StateChangeEvent[];
  alarms: BrowserAlarm[];
  weather: WeatherData | null;
}

export function useStokerWebSocket() {
  const clientRef = useRef<Client | null>(null);
  const [state, setState] = useState<StokerWebSocketState>({
    connected: false,
    latestReadings: {},
    temperatureHistory: {},
    events: [],
    alarms: [],
    weather: null,
  });

  const handleTemperatures = useCallback((msg: IMessage) => {
    // Backend sends event.dataPoints directly as a JSON array, not the wrapper object
    const dataPoints: SDataPoint[] = JSON.parse(msg.body);
    setState(prev => {
      const readings = { ...prev.latestReadings };
      const history = { ...prev.temperatureHistory };

      for (const dp of dataPoints) {
        readings[dp.deviceId] = dp;

        // Fall back to field presence if @JsonTypeInfo discriminator is absent
        const isProbe = dp.type === 'probe' || (!dp.type && 'tempF' in dp);
        if (isProbe) {
          const probe = dp as SProbeDataPoint;
          const existing = history[dp.deviceId] ?? [];
          const updated = [...existing, probe];
          history[dp.deviceId] = updated.length > MAX_HISTORY
            ? updated.slice(updated.length - MAX_HISTORY)
            : updated;
        }
      }

      return { ...prev, latestReadings: readings, temperatureHistory: history };
    });
  }, []);

  const handleEvents = useCallback((msg: IMessage) => {
    const event: StateChangeEvent = JSON.parse(msg.body);
    setState(prev => ({
      ...prev,
      events: [...prev.events.slice(-49), event],
    }));
  }, []);

  const handleAlarms = useCallback((msg: IMessage) => {
    const alarm: BrowserAlarm = JSON.parse(msg.body);
    setState(prev => ({
      ...prev,
      alarms: [...prev.alarms, alarm],
    }));
  }, []);

  const handleWeather = useCallback((msg: IMessage) => {
    const weather: WeatherData = JSON.parse(msg.body);
    setState(prev => ({ ...prev, weather }));
  }, []);

  const dismissAlarm = useCallback((index: number) => {
    setState(prev => ({
      ...prev,
      alarms: prev.alarms.filter((_, i) => i !== index),
    }));
  }, []);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        setState(prev => ({ ...prev, connected: true }));
        client.subscribe('/topic/temperatures', handleTemperatures);
        client.subscribe('/topic/events', handleEvents);
        client.subscribe('/topic/alarms', handleAlarms);
        client.subscribe('/topic/weather', handleWeather);
      },
      onDisconnect: () => {
        setState(prev => ({ ...prev, connected: false }));
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
        setState(prev => ({ ...prev, connected: false }));
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      client.deactivate();
    };
  }, [handleTemperatures, handleEvents, handleAlarms, handleWeather]);

  return { ...state, dismissAlarm };
}
