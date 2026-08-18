import React, { useState, useRef, useEffect } from 'react';
import { Bell, CheckCheck, X } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationService, Notification } from '@/services/notificationService';

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const qc = useQueryClient();

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const { data: notifData } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationService.getAll().then(r => r.data),
    refetchInterval: 30000,
  });

  const notifications: Notification[] = notifData?.data || [];
  const unreadCount = notifications.filter(n => !n.read).length;

  const markRead = useMutation({
    mutationFn: (id: number) => notificationService.markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const markAll = useMutation({
    mutationFn: () => notificationService.markAllRead(),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  });

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen(!open)}
        className="relative text-muted-foreground hover:text-white transition-colors"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 w-4 h-4 bg-primary rounded-full text-[10px] font-bold text-white flex items-center justify-center">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute top-full right-0 mt-2 w-80 bg-[#111827] border border-border rounded-xl shadow-2xl z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-border">
            <h3 className="text-sm font-semibold text-white">Notifications</h3>
            <div className="flex items-center gap-2">
              {unreadCount > 0 && (
                <button
                  onClick={() => markAll.mutate()}
                  className="text-xs text-primary hover:text-blue-400 flex items-center gap-1 transition-colors"
                >
                  <CheckCheck className="w-3.5 h-3.5" /> Mark all read
                </button>
              )}
              <button onClick={() => setOpen(false)} className="text-muted-foreground hover:text-white transition-colors">
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="max-h-80 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="p-8 text-center">
                <Bell className="w-8 h-8 text-muted-foreground mx-auto mb-2 opacity-50" />
                <p className="text-sm text-muted-foreground">No notifications</p>
              </div>
            ) : (
              notifications.map(n => (
                <div
                  key={n.id}
                  onClick={() => !n.read && markRead.mutate(n.id)}
                  className={`px-4 py-3 border-b border-border/50 last:border-0 cursor-pointer hover:bg-white/5 transition-colors ${!n.read ? 'bg-primary/5' : ''}`}
                >
                  <div className="flex items-start gap-3">
                    {!n.read && (
                      <div className="w-2 h-2 rounded-full bg-primary mt-1.5 flex-shrink-0" />
                    )}
                    <div className={!n.read ? '' : 'ml-5'}>
                      <p className="text-sm font-medium text-white">{n.title}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">{n.message}</p>
                      <p className="text-xs text-muted-foreground/60 mt-1">
                        {new Date(n.createdAt).toLocaleDateString()} {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
