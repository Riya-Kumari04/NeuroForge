import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { QueryClient } from '@tanstack/react-query';

export interface TaskBoardEvent {
  taskId: number;
  projectId: number;
  previousStatus: string;
  newStatus: string;
  changedBy: string;
  timestamp: string;
}

class WebSocketService {
  private stompClient: Client | null = null;
  private subscriptions: Map<string, any> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  /**
   * Connect to the WebSocket server
   * @param token JWT token for authentication
   */
  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.stompClient && this.stompClient.connected) {
        resolve();
        return;
      }

      const socket = new SockJS(`${import.meta.env.VITE_API_URL}/ws/board`);
      
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log('[WebSocket]', str);
          }
        },
        reconnectDelay: this.reconnectDelay,
        onConnect: () => {
          console.log('[WebSocket] Connected');
          this.reconnectAttempts = 0;
          resolve();
        },
        onStompError: (frame) => {
          console.error('[WebSocket] STOMP error:', frame);
          reject(frame);
        },
        onWebSocketClose: () => {
          console.log('[WebSocket] Disconnected');
          this.handleReconnect(token);
        },
      });

      this.stompClient.activate();
    });
  }

  /**
   * Subscribe to project board updates
   * @param projectId Project ID to subscribe to
   * @param queryClient React Query client for cache updates
   * @param callback Optional callback for event handling
   */
  subscribeToProjectBoard(
    projectId: number,
    queryClient: QueryClient,
    callback?: (event: TaskBoardEvent) => void
  ): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('[WebSocket] Not connected, cannot subscribe');
      return;
    }

    const destination = `/topic/project/${projectId}/board`;
    
    // Unsubscribe if already subscribed
    if (this.subscriptions.has(destination)) {
      this.unsubscribeFromProjectBoard(projectId);
    }

    const subscription = this.stompClient.subscribe(destination, (message) => {
      try {
        const event: TaskBoardEvent = JSON.parse(message.body);
        console.log('[WebSocket] Received task board event:', event);

        // Update React Query cache with a new reference to trigger re-render
        queryClient.setQueryData(['project-tasks', projectId], (old: any) => {
          if (!old) return old;
          
          // Handle both wrapped and unwrapped data structures
          const tasks = Array.isArray(old) ? old : (old.data || []);
          
          // Create a new array with updated task to ensure reference change
          const updatedTasks = tasks.map((task: any) => {
            if (task.id === event.taskId) {
              // Create a new object with updated status
              return { ...task, status: event.newStatus };
            }
            return task;
          });
          
          // Return with new object reference
          if (Array.isArray(old)) {
            return [...updatedTasks];
          } else {
            return { 
              ...old, 
              data: [...updatedTasks] 
            };
          }
        });

        // Call custom callback if provided
        if (callback) {
          callback(event);
        }
      } catch (error) {
        console.error('[WebSocket] Error parsing message:', error);
      }
    });

    this.subscriptions.set(destination, subscription);
    console.log(`[WebSocket] Subscribed to ${destination}`);
  }

  /**
   * Unsubscribe from project board updates
   * @param projectId Project ID to unsubscribe from
   */
  unsubscribeFromProjectBoard(projectId: number): void {
    const destination = `/topic/project/${projectId}/board`;
    const subscription = this.subscriptions.get(destination);
    
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
      console.log(`[WebSocket] Unsubscribed from ${destination}`);
    }
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
      this.subscriptions.clear();
      console.log('[WebSocket] Disconnected');
    }
  }

  /**
   * Handle reconnection logic
   * @param token JWT token for authentication
   */
  private handleReconnect(token: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`[WebSocket] Reconnecting... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
      
      setTimeout(() => {
        this.connect(token).catch((error) => {
          console.error('[WebSocket] Reconnection failed:', error);
        });
      }, this.reconnectDelay);
    } else {
      console.error('[WebSocket] Max reconnection attempts reached');
    }
  }

  /**
   * Check if WebSocket is connected
   */
  isConnected(): boolean {
    return this.stompClient?.connected ?? false;
  }
}

// Export singleton instance
export const websocketService = new WebSocketService();
