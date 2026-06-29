import { createContext, useContext, useEffect, useRef, useCallback, useState, type ReactNode } from 'react';
import { Client, type IMessage, type IFrame } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getStoredToken } from '@/shared/utils/auth';

interface WebSocketContextValue {
  subscribe: (topic: string, callback: (msg: IMessage) => void) => () => void;
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextValue | undefined>(undefined);

export const WebSocketProvider = ({ children }: { children: ReactNode }) => {
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<Map<string, ReturnType<Client['subscribe']>>>(new Map());
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const token = getStoredToken();
    if (!token) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        setIsConnected(true);
        // eslint-disable-next-line no-console
        console.info('[WebSocket] Connected to ForgeMind STOMP broker');
      },
      onDisconnect: () => {
        setIsConnected(false);
        // eslint-disable-next-line no-console
        console.info('[WebSocket] Disconnected');
      },
      onStompError: (frame: IFrame) => {
        console.error('[WebSocket] STOMP error:', frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, []);

  const subscribe = useCallback((topic: string, callback: (msg: IMessage) => void) => {
    const client = clientRef.current;
    if (!client?.connected) {
      console.warn(`[WebSocket] Cannot subscribe to ${topic}: not connected`);
      return () => {};
    }

    const existing = subscriptionsRef.current.get(topic);
    if (existing) existing.unsubscribe();

    const sub = client.subscribe(topic, callback);
    subscriptionsRef.current.set(topic, sub);

    return () => {
      sub.unsubscribe();
      subscriptionsRef.current.delete(topic);
    };
  }, []);

  return (
    <WebSocketContext.Provider value={{ subscribe, isConnected }}>
      {children}
    </WebSocketContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useWebSocket = () => {
  const ctx = useContext(WebSocketContext);
  if (!ctx) throw new Error('useWebSocket must be used within WebSocketProvider');
  return ctx;
};

/**
 * Convenience hook: subscribes to a topic and runs a callback on each message.
 * Automatically unsubscribes when the component unmounts.
 */
// eslint-disable-next-line react-refresh/only-export-components
export const useStompSubscription = (
  topic: string,
  callback: (msg: IMessage) => void,
  enabled = true
) => {
  const { subscribe } = useWebSocket();

  useEffect(() => {
    if (!enabled) return;
    return subscribe(topic, callback);
  }, [topic, callback, subscribe, enabled]);
};
