// src/hooks/useWebSocket.js
import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import { getToken } from "../services/localStorageService";
import { CONFIG } from "../configurations/configuration";

export function useWebSocket(onMessage) {
  const clientRef = useRef(null);
  // Dùng ref để tránh stale closure — luôn gọi phiên bản mới nhất của callback
  const onMessageRef = useRef(onMessage);
  onMessageRef.current = onMessage;

  useEffect(() => {
    const token = getToken();

    if (!token) {
      console.warn("No token found, skipping WebSocket connection");
      return;
    }

    const client = new Client({
      brokerURL: `${CONFIG.WS_CHAT}`,

      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      onConnect: () => {
        console.log("STOMP connected");

        client.subscribe("/user/queue/messages", (frame) => {
          console.log("STOMP message received:", frame.body);
          try {
            const message = JSON.parse(frame.body);
            onMessageRef.current(message);
          } catch (e) {
            console.error("Failed to parse STOMP message:", e);
          }
        });
      },

      onDisconnect: () => {
        console.log("STOMP disconnected");
      },

      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers?.message, frame.body);
      },

      onWebSocketError: (event) => {
        console.error("WebSocket error:", event);
      },

      reconnectDelay: 5000,
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, []); // Chạy 1 lần, onMessageRef handle việc cập nhật callback

  return clientRef;
}
