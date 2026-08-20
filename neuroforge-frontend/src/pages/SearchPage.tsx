import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useLocation } from 'wouter';
import { Search, FolderKanban, Users, Building2, GitBranch, CheckSquare, Loader2, X } from 'lucide-react';
import Sidebar from '@/components/common/Sidebar';
import DashboardNavbar from '@/components/common/DashboardNavbar';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';

interface SearchResults {
  users?: any[];
  organizations?: any[];
  projects?: any[];
  tasks?: any[];
  sprints?: any[];
  teams?: any[];
}

interface Suggestion {
  type: string;
  id: number;
  title: string;
  subtitle: string;
}

export default function SearchPage() {
  const { role } = useAuth();
  const [, setLocation] = useLocation();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResults | null>(null);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [loadingSuggestions, setLoadingSuggestions] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);
  const debounceRef = useRef<NodeJS.Timeout>();

  // Re-run search whenever the URL changes (e.g. navigating from the top
  // navbar search while already on this page).
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const q = params.get('q') || '';
    if (q) { setQuery(q); doSearch(q); }
  }, [location]);

  const doSearch = useCallback(async (q: string) => {
    if (!q.trim()) return;
    setLoading(true);
    setSearched(true);
    setShowSuggestions(false);
    try {
      const res = await api.get<any>(`/search?q=${encodeURIComponent(q)}`);
      setResults(res.data?.data || {});
    } catch {
      setResults({});
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchSuggestions = useCallback(async (q: string) => {
    if (q.length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }

    setLoadingSuggestions(true);
    try {
      const res = await api.get<any>(`/search/suggestions?q=${encodeURIComponent(q)}`);
      setSuggestions(res.data?.data || []);
      setShowSuggestions(true);
    } catch {
      setSuggestions([]);
    } finally {
      setLoadingSuggestions(false);
    }
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setQuery(value);

    // Debounce suggestions
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      fetchSuggestions(value);
    }, 300);
  };

  const handleSuggestionClick = (suggestion: Suggestion) => {
    setQuery(suggestion.title);
    setShowSuggestions(false);
    doSearch(suggestion.title);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLocation(`/${role}/search?q=${encodeURIComponent(query)}`);
    doSearch(query);
  };

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const total = results
    ? Object.values(results).reduce((s, arr) => s + (arr?.length || 0), 0)
    : 0;

  return (
    <div className="min-h-screen bg-background flex">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        <DashboardNavbar title="Search" />
        <main className="flex-1 p-8 overflow-y-auto">
          <h2 className="text-2xl font-bold text-white mb-6">Search</h2>

          <form onSubmit={handleSubmit} className="mb-8">
            <div className="relative max-w-xl" ref={searchRef}>
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                type="text"
                value={query}
                onChange={handleInputChange}
                onFocus={() => query.length >= 2 && setShowSuggestions(true)}
                placeholder="Search users, projects, organizations, tasks…"
                className="w-full bg-card border border-border rounded-lg pl-10 pr-12 py-2.5 text-sm text-white focus:outline-none focus:ring-1 focus:ring-primary"
              />
              {query && (
                <button
                  type="button"
                  onClick={() => { setQuery(''); setSuggestions([]); setShowSuggestions(false); }}
                  className="absolute right-12 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-white"
                >
                  <X className="w-4 h-4" />
                </button>
              )}
              <button type="submit" className="absolute right-2 top-1/2 -translate-y-1/2 bg-primary text-white text-xs font-medium px-3 py-1.5 rounded-md hover:bg-primary/90 transition-colors">
                Search
              </button>

              {/* Suggestions Dropdown */}
              {showSuggestions && (suggestions.length > 0 || loadingSuggestions) && (
                <div className="absolute top-full left-0 right-0 mt-2 bg-card border border-border rounded-lg shadow-xl max-h-64 overflow-y-auto z-50">
                  {loadingSuggestions ? (
                    <div className="flex items-center justify-center py-4">
                      <Loader2 className="w-4 h-4 animate-spin text-primary" />
                    </div>
                  ) : suggestions.length === 0 ? (
                    <div className="p-4 text-sm text-muted-foreground text-center">No suggestions found</div>
                  ) : (
                    suggestions.map((suggestion, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => handleSuggestionClick(suggestion)}
                        className="w-full text-left px-4 py-3 hover:bg-white/5 transition-colors border-b border-border/50 last:border-0"
                      >
                        <div className="flex items-center gap-3">
                          {suggestion.type === 'project' && <FolderKanban className="w-4 h-4 text-primary" />}
                          {suggestion.type === 'task' && <CheckSquare className="w-4 h-4 text-green-400" />}
                          {suggestion.type === 'sprint' && <GitBranch className="w-4 h-4 text-blue-400" />}
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-white truncate">{suggestion.title}</p>
                            <p className="text-xs text-muted-foreground truncate">{suggestion.subtitle}</p>
                          </div>
                        </div>
                      </button>
                    ))
                  )}
                </div>
              )}
            </div>
          </form>

          {loading && (
            <div className="flex items-center justify-center py-16"><Loader2 className="w-6 h-6 animate-spin text-primary" /></div>
          )}

          {!loading && searched && results && total === 0 && (
            <div className="text-center py-16">
              <Search className="w-10 h-10 text-muted-foreground mx-auto mb-3 opacity-50" />
              <p className="text-sm text-muted-foreground">No results found for "{query}"</p>
            </div>
          )}

          {!loading && results && total > 0 && (
            <div className="space-y-6">
              <p className="text-sm text-muted-foreground">{total} result{total !== 1 ? 's' : ''} for "{query}"</p>

              {results.users && results.users.length > 0 && (
                <ResultSection title="Users" icon={Users} items={results.users.map((u: any) => ({
                  primary: u.name, secondary: u.email, badge: u.role?.replace('ROLE_', '')
                }))} />
              )}
              {results.organizations && results.organizations.length > 0 && (
                <ResultSection title="Organizations" icon={Building2} items={results.organizations.map((o: any) => ({
                  primary: o.name, secondary: o.industry || o.plan, badge: o.plan
                }))} />
              )}
              {results.projects && results.projects.length > 0 && (
                <ResultSection title="Projects" icon={FolderKanban} items={results.projects.map((p: any) => ({
                  primary: p.projectName, secondary: p.organizationName, badge: p.status
                }))} />
              )}
              {results.teams && results.teams.length > 0 && (
                <ResultSection title="Teams" icon={Users} items={results.teams.map((t: any) => ({
                  primary: t.name, secondary: t.description, badge: undefined
                }))} />
              )}
              {results.sprints && results.sprints.length > 0 && (
                <ResultSection title="Sprints" icon={GitBranch} items={results.sprints.map((s: any) => ({
                  primary: s.name, secondary: s.goal, badge: s.status
                }))} />
              )}
              {results.tasks && results.tasks.length > 0 && (
                <ResultSection title="Tasks" icon={CheckSquare} items={results.tasks.map((t: any) => ({
                  primary: t.title, secondary: t.description, badge: t.status
                }))} />
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

function ResultSection({ title, icon: Icon, items }: {
  title: string;
  icon: React.ElementType;
  items: { primary: string; secondary?: string; badge?: string }[];
}) {
  return (
    <div className="bg-card border border-border rounded-xl overflow-hidden">
      <div className="flex items-center gap-2 px-5 py-3 border-b border-border">
        <Icon className="w-4 h-4 text-muted-foreground" />
        <h3 className="text-sm font-semibold text-white">{title}</h3>
        <span className="ml-auto text-xs text-muted-foreground">{items.length}</span>
      </div>
      <div className="divide-y divide-border/50">
        {items.map((item, i) => (
          <div key={i} className="flex items-center gap-3 px-5 py-3 hover:bg-white/5 transition-colors">
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{item.primary}</p>
              {item.secondary && <p className="text-xs text-muted-foreground truncate">{item.secondary}</p>}
            </div>
            {item.badge && (
              <span className="text-xs px-2 py-0.5 rounded border bg-slate-500/10 text-slate-400 border-slate-500/20 flex-shrink-0">
                {item.badge}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
