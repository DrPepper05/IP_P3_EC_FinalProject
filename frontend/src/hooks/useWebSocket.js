import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WEBSOCKET_URL = 'http://localhost:8080/ws';

export const useWebSocket = (topic, onMessage) => {
  const client = useRef(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    // Create STOMP client
    client.current = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      debug: (str) => {
        console.log('[WebSocket]', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // Connection established
    client.current.onConnect = () => {
      console.log('WebSocket connected');
      setConnected(true);

      // Subscribe to topic
      if (topic) {
        client.current.subscribe(topic, (message) => {
          if (onMessage) {
            const parsedMessage = JSON.parse(message.body);
            onMessage(parsedMessage);
          }
        });
      }
    };

    // Connection error
    client.current.onStompError = (frame) => {
      console.error('WebSocket error:', frame);
      setConnected(false);
    };

    // Activate connection
    client.current.activate();

    // Cleanup on unmount
    return () => {
      if (client.current) {
        client.current.deactivate();
      }
    };
  }, [topic, onMessage]);

  return { connected };
};
