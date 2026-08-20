import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useLocation } from 'wouter';
import { FileText, Plus, Clock, CheckCircle, AlertCircle, Eye, Edit2, Search, Trash2, X, Sparkles } from 'lucide-react';
import { specificationService, Specification } from '@/services/specificationService';
import { projectService, Project, Task } from '@/services/projectService';
import Modal from '@/components/common/Modal';
import Loader from '@/components/common/Loader';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/context/AuthContext';
import { canManageProjects } from '@/lib/roleUtils';

interface Props {
  project: Project;
}

function RequirementForm({ spec, isEdit, onSuccess, onCancel }: {
  spec?: Specification | null;
  isEdit: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  
  const [form, setForm] = useState({
    title: spec?.title || '',
    description: '',
    userStories: '',
    acceptanceCriteria: '',
    functionalRequirements: '',
    nonFunctionalRequirements: '',
  });

  const mutation = useMutation({
    mutationFn: () => isEdit
      ? specificationService.update(spec!.id, {
          description: form.description,
          userStories: form.userStories,
          acceptanceCriteria: form.acceptanceCriteria,
          functionalRequirements: form.functionalRequirements,
          nonFunctionalRequirements: form.nonFunctionalRequirements,
        })
      : specificationService.create({
          title: form.title,
          description: form.description,
          userStories: form.userStories,
          acceptanceCriteria: form.acceptanceCriteria,
          functionalRequirements: form.functionalRequirements,
          nonFunctionalRequirements: form.nonFunctionalRequirements,
        }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['specifications'] });
      toast({ title: isEdit ? 'Requirement updated' : 'Requirement created' });
      onSuccess();
    },
    onError: () => toast({ title: 'Error', description: 'Operation failed.', variant: 'destructive' }),
  });

  const inputClass = 'w-full bg-background border border-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all';

  return (
    <div className="space-y-4">
      {!isEdit && (
        <div>
          <label className="text-xs font-medium text-white block mb-1.5">Title</label>
          <input
            className={inputClass}
            value={form.title}
            onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
            placeholder="e.g. User Authentication System"
          />
        </div>
      )}
      <div>
        <label className="text-xs font-medium text-white block mb-1.5">Description</label>
        <textarea
          className={inputClass}
          rows={3}
          value={form.description}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
          placeholder="Requirement description..."
        />
      </div>
      <div>
        <label className="text-xs font-medium text-white block mb-1.5">Functional Requirements</label>
        <textarea
          className={inputClass}
          rows={2}
          value={form.functionalRequirements}
          onChange={e => setForm(f => ({ ...f, functionalRequirements: e.target.value }))}
          placeholder="Functional requirements..."
        />
      </div>
      <div>
        <label className="text-xs font-medium text-white block mb-1.5">Non-Functional Requirements</label>
        <textarea
          className={inputClass}
          rows={2}
          value={form.nonFunctionalRequirements}
          onChange={e => setForm(f => ({ ...f, nonFunctionalRequirements: e.target.value }))}
          placeholder="Performance, security, etc..."
        />
      </div>
      <div>
        <label className="text-xs font-medium text-white block mb-1.5">User Stories</label>
        <textarea
          className={inputClass}
          rows={2}
          value={form.userStories}
          onChange={e => setForm(f => ({ ...f, userStories: e.target.value }))}
          placeholder="As a user, I want to..."
        />
      </div>
      <div>
        <label className="text-xs font-medium text-white block mb-1.5">Acceptance Criteria</label>
        <textarea
          className={inputClass}
          rows={2}
          value={form.acceptanceCriteria}
          onChange={e => setForm(f => ({ ...f, acceptanceCriteria: e.target.value }))}
          placeholder="Given/When/Then criteria..."
        />
      </div>
      <div className="flex gap-3 pt-2">
        <button
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending || (!isEdit && !form.title)}
          className="flex-1 bg-primary text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {mutation.isPending ? 'Saving...' : (isEdit ? 'Update Requirement' : 'Create Requirement')}
        </button>
        <button
          onClick={onCancel}
          className="px-4 py-2 text-sm rounded-lg border border-border text-muted-foreground hover:text-white hover:border-primary/50 transition-colors"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}

export default function ProjectRequirementsTab({ project }: Props) {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const { role } = useAuth();
  const [, setLocation] = useLocation();
  const canManage = canManageProjects(role);
  
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedSpec, setSelectedSpec] = useState<Specification | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const { data: specsData, isLoading: specsLoading } = useQuery({
    queryKey: ['specifications'],
    queryFn: () => specificationService.getAll().then((r: any) => r.data.data),
  });

  const { data: tasksData } = useQuery({
    queryKey: ['project-tasks', project.id],
    queryFn: () => projectService.getTasksByProject(project.id).then((r: any) => r.data),
  });

  const specs = Array.isArray(specsData?.content) ? specsData.content : (Array.isArray(specsData) ? specsData : []);
  const tasks = Array.isArray(tasksData) ? tasksData : [];

  const filteredSpecs = specs.filter((spec: Specification) => {
    const matchesSearch = !searchTerm || 
      spec.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      spec.specificationKey.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || spec.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getSpecStats = (specId: string) => {
    const specTasks = tasks.filter((t: Task) => t.specificationId === specId);
    const completedTasks = specTasks.filter((t: Task) => t.status === 'DONE');
    const total = specTasks.length;
    const completed = completedTasks.length;
    const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;
    return { total, completed, percentage };
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'APPROVED': return <CheckCircle className="w-4 h-4 text-emerald-400" />;
      case 'IN_REVIEW': return <Clock className="w-4 h-4 text-amber-400" />;
      case 'DRAFT': return <Edit2 className="w-4 h-4 text-slate-400" />;
      case 'REJECTED': return <AlertCircle className="w-4 h-4 text-red-400" />;
      default: return <FileText className="w-4 h-4 text-slate-400" />;
    }
  };

  const formatDate = (dt?: string) =>
    dt ? new Date(dt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '—';

  if (specsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-white">Requirements</h2>
          <p className="text-sm text-muted-foreground mt-1">
            Manage project specifications and track requirement traceability
          </p>
        </div>
        {canManage && (
          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                const basePath = role === 'org-admin' ? '/org-admin' : '/project-manager';
                setLocation(`${basePath}/specifications/generate`);
              }}
              className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 transition-all"
            >
              <Sparkles className="w-4 h-4" />
              <span className="text-sm font-medium">Generate with AI</span>
            </button>
            <button
              onClick={() => setShowCreateModal(true)}
              className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors"
            >
              <Plus className="w-4 h-4" />
              <span className="text-sm font-medium">New Requirement</span>
            </button>
          </div>
        )}
      </div>

      {/* Search and Filters */}
      <div className="flex items-center gap-3">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            className="w-full bg-background border border-border rounded-lg pl-9 pr-3 py-2 text-sm text-white placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-all"
            placeholder="Search requirements..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
          />
        </div>
        <select
          className="bg-background border border-border rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-primary transition-all"
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
        >
          <option value="ALL">All Statuses</option>
          <option value="DRAFT">Draft</option>
          <option value="IN_REVIEW">In Review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="ARCHIVED">Archived</option>
        </select>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: 'Total Requirements', value: specs.length, icon: FileText, color: 'text-blue-400', bg: 'bg-blue-500/10' },
          { label: 'Approved', value: specs.filter((s: Specification) => s.status === 'APPROVED').length, icon: CheckCircle, color: 'text-emerald-400', bg: 'bg-emerald-500/10' },
          { label: 'Linked Tasks', value: tasks.filter((t: Task) => t.specificationId).length, icon: Eye, color: 'text-purple-400', bg: 'bg-purple-500/10' },
          { label: 'In Review', value: specs.filter((s: Specification) => s.status === 'IN_REVIEW').length, icon: Clock, color: 'text-amber-400', bg: 'bg-amber-500/10' },
        ].map((card: any) => (
          <div key={card.label} className="bg-card border border-border rounded-xl p-4">
            <div className={`w-9 h-9 rounded-lg ${card.bg} flex items-center justify-center mb-3`}>
              <card.icon className={`w-4 h-4 ${card.color}`} />
            </div>
            <p className="text-muted-foreground text-xs font-medium mb-1">{card.label}</p>
            <p className="text-2xl font-bold text-white">{card.value}</p>
          </div>
        ))}
      </div>

      {/* Requirements List */}
      <div className="bg-card border border-border rounded-xl overflow-hidden">
        <div className="p-5 border-b border-border">
          <h3 className="text-sm font-semibold text-white">All Specifications ({filteredSpecs.length})</h3>
        </div>
        
        {filteredSpecs.length === 0 ? (
          <div className="p-12 text-center">
            <FileText className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground mb-4">No specifications found</p>
            {canManage && (
              <button
                onClick={() => setShowCreateModal(true)}
                className="text-primary hover:text-primary/80 text-sm font-medium"
              >
                Create your first requirement
              </button>
            )}
          </div>
        ) : (
          <div className="divide-y divide-border">
            {filteredSpecs.map((spec: Specification) => {
              const stats = getSpecStats(spec.id);
              return (
                <div key={spec.id} className="p-5 hover:bg-background/50 transition-colors">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-2">
                        {getStatusIcon(spec.status)}
                        <h4 className="font-semibold text-white truncate">{spec.title}</h4>
                        <span className="text-xs text-muted-foreground bg-background px-2 py-1 rounded">
                          {spec.specificationKey}
                        </span>
                      </div>
                      <p className="text-sm text-muted-foreground mb-3 line-clamp-2">
                        Version {spec.currentVersion} • {spec.status.replace('_', ' ')}
                      </p>
                      <div className="flex items-center gap-6 text-xs text-muted-foreground">
                        <span>Updated {formatDate(spec.updatedAt)}</span>
                        <span>Created {formatDate(spec.createdAt)}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-4 flex-shrink-0">
                      <div className="text-right">
                        <p className="text-sm font-medium text-white">{stats.completed}/{stats.total}</p>
                        <p className="text-xs text-muted-foreground">Tasks</p>
                      </div>
                      <div className="w-24">
                        <div className="h-2 bg-background rounded-full overflow-hidden">
                          <div 
                            className="h-full bg-emerald-500 transition-all"
                            style={{ width: `${stats.percentage}%` }}
                          />
                        </div>
                        <p className="text-xs text-muted-foreground mt-1 text-right">{stats.percentage}%</p>
                      </div>
                      {canManage && (
                        <div className="flex items-center gap-2">
                          {spec.status === 'DRAFT' && (
                            <button
                              onClick={() => {
                                specificationService.submitForReview(spec.id, spec.currentVersion).then(() => {
                                  queryClient.invalidateQueries({ queryKey: ['specifications'] });
                                  toast({ title: 'Submitted for review' });
                                });
                              }}
                              className="px-2 py-1 text-xs bg-amber-500/20 text-amber-400 rounded hover:bg-amber-500/30 transition-colors"
                            >
                              Submit
                            </button>
                          )}
                          {spec.status === 'IN_REVIEW' && (
                            <>
                              <button
                                onClick={() => {
                                  specificationService.approve(spec.id, spec.currentVersion).then(() => {
                                    queryClient.invalidateQueries({ queryKey: ['specifications'] });
                                    toast({ title: 'Requirement approved' });
                                  });
                                }}
                                className="px-2 py-1 text-xs bg-emerald-500/20 text-emerald-400 rounded hover:bg-emerald-500/30 transition-colors"
                              >
                                Approve
                              </button>
                              <button
                                onClick={() => {
                                  specificationService.reject(spec.id, spec.currentVersion, { comment: 'Rejected' }).then(() => {
                                    queryClient.invalidateQueries({ queryKey: ['specifications'] });
                                    toast({ title: 'Requirement rejected' });
                                  });
                                }}
                                className="px-2 py-1 text-xs bg-red-500/20 text-red-400 rounded hover:bg-red-500/30 transition-colors"
                              >
                                Reject
                              </button>
                            </>
                          )}
                          <button
                            onClick={() => {
                              setSelectedSpec(spec);
                              setShowEditModal(true);
                            }}
                            className="p-1.5 rounded hover:bg-white/10 text-muted-foreground hover:text-white transition-colors"
                            title="Edit"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Create/Edit Modal */}
      <Modal 
        isOpen={showCreateModal || showEditModal} 
        onClose={() => {
          setShowCreateModal(false);
          setShowEditModal(false);
          setSelectedSpec(null);
        }} 
        title={showEditModal ? 'Edit Requirement' : 'Create New Requirement'}
      >
        <RequirementForm 
          spec={selectedSpec} 
          projectId={project.id}
          isEdit={showEditModal}
          onSuccess={() => {
            setShowCreateModal(false);
            setShowEditModal(false);
            setSelectedSpec(null);
            queryClient.invalidateQueries({ queryKey: ['specifications'] });
          }}
          onCancel={() => {
            setShowCreateModal(false);
            setShowEditModal(false);
            setSelectedSpec(null);
          }}
        />
      </Modal>
    </div>
  );
}
