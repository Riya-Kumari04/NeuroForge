import React from 'react';
import { Search } from 'lucide-react';

interface FilterOption {
  value: string;
  label: string;
}

interface SearchFilterBarProps {
  search: string;
  onSearchChange: (v: string) => void;
  filters?: { label: string; value: string; options: FilterOption[]; onChange: (v: string) => void }[];
  placeholder?: string;
}

export default function SearchFilterBar({ search, onSearchChange, filters = [], placeholder = 'Search...' }: SearchFilterBarProps) {
  return (
    <div className="flex flex-col sm:flex-row gap-3">
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <input
          type="text"
          value={search}
          onChange={e => onSearchChange(e.target.value)}
          placeholder={placeholder}
          className="w-full bg-background border border-border rounded-lg pl-10 pr-4 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
        />
      </div>
      {filters.map(f => (
        <select
          key={f.label}
          value={f.value}
          onChange={e => f.onChange(e.target.value)}
          className="bg-background border border-border rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-primary transition-all"
        >
          <option value="">{f.label}</option>
          {f.options.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      ))}
    </div>
  );
}
