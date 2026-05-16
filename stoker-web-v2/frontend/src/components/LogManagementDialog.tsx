import { useState, useEffect, useCallback } from 'react';
import Modal from './Modal';
import {
  getActiveLogs, startLog, stopLog, getLogNotes, addLogNote,
  getLogFiles, loadLog, closeViewLog,
} from '../api/stokerApi';
import type { LogItem, LogNote, LogFileEntry } from '../types/stoker';

interface LogManagementDialogProps {
  cookerNames: string[];
  onClose: () => void;
}

type Tab = 'active' | 'browse';

export default function LogManagementDialog({ cookerNames, onClose }: LogManagementDialogProps) {
  const [tab, setTab] = useState<Tab>('active');

  // ── Active logs state ──────────────────────────────────────────
  const [activeLogs, setActiveLogs] = useState<LogItem[]>([]);
  const [selectedLog, setSelectedLog] = useState<string | null>(null);
  const [notes, setNotes] = useState<LogNote[]>([]);
  const [newNote, setNewNote] = useState('');
  const [logName, setLogName] = useState('');
  const [cookerName, setCookerName] = useState(cookerNames[0] ?? '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ── Browse logs state ──────────────────────────────────────────
  const [logFiles, setLogFiles] = useState<LogFileEntry[]>([]);
  const [filesLoading, setFilesLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<LogFileEntry | null>(null);
  const [resumeConfirm, setResumeConfirm] = useState(false);

  const refreshLogs = useCallback(async () => {
    try {
      const logs = await getActiveLogs();
      setActiveLogs(logs);
    } catch {
      setError('Failed to load active logs.');
    }
  }, []);

  useEffect(() => {
    refreshLogs();
  }, [refreshLogs]);

  useEffect(() => {
    if (!selectedLog) { setNotes([]); return; }
    getLogNotes(selectedLog).then(setNotes).catch(() => {});
  }, [selectedLog]);

  const loadBrowseTab = useCallback(async () => {
    setFilesLoading(true);
    try {
      const files = await getLogFiles();
      setLogFiles(files);
    } catch {
      setError('Failed to load log files.');
    } finally {
      setFilesLoading(false);
    }
  }, []);

  useEffect(() => {
    if (tab === 'browse') loadBrowseTab();
  }, [tab, loadBrowseTab]);

  // ── Active log handlers ────────────────────────────────────────

  const handleStart = async () => {
    if (!logName.trim() || !cookerName) return;
    setLoading(true); setError(null);
    try {
      await startLog(cookerName, logName.trim());
      setLogName('');
      await refreshLogs();
    } catch (e: any) {
      setError(e.message || 'Failed to start log.');
    } finally {
      setLoading(false);
    }
  };

  const handleStop = async (name: string) => {
    setLoading(true); setError(null);
    try {
      await stopLog(name);
      if (selectedLog === name) setSelectedLog(null);
      await refreshLogs();
    } catch (e: any) {
      setError(e.message || 'Failed to stop log.');
    } finally {
      setLoading(false);
    }
  };

  const handleCloseView = async (name: string) => {
    setLoading(true); setError(null);
    try {
      await closeViewLog(name);
      if (selectedLog === name) setSelectedLog(null);
      await refreshLogs();
    } catch (e: any) {
      setError(e.message || 'Failed to close log.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddNote = async () => {
    if (!newNote.trim() || !selectedLog) return;
    const text = newNote.trim();
    try {
      await addLogNote(selectedLog, text);
      const note: LogNote = { noteDate: new Date().toISOString(), note: text };
      setNotes(prev => [...prev, note]);
      setNewNote('');
    } catch {
      setError('Failed to add note.');
    }
  };

  // ── Browse log handlers ────────────────────────────────────────

  const handleView = async (file: LogFileEntry) => {
    setError(null);
    try {
      await loadLog(file.filePath, false);
      await refreshLogs();
      setTab('active');
    } catch (e: any) {
      setError(e.message || 'Failed to load log.');
    }
  };

  const handleResume = async (file: LogFileEntry) => {
    setSelectedFile(file);
    setResumeConfirm(true);
  };

  const confirmResume = async () => {
    if (!selectedFile) return;
    setResumeConfirm(false);
    setError(null);
    try {
      await loadLog(selectedFile.filePath, true);
      await refreshLogs();
      setTab('active');
    } catch (e: any) {
      setError(e.message || 'Failed to resume log.');
    } finally {
      setSelectedFile(null);
    }
  };

  const formatSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  };

  return (
    <Modal title="Cook Log Management" onClose={onClose} width="max-w-2xl">
      <div className="p-5 space-y-4">
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 rounded px-3 py-2 text-sm">
            {error}
          </div>
        )}

        {/* Tab bar */}
        <div className="flex border-b border-gray-700">
          {(['active', 'browse'] as Tab[]).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`px-4 py-2 text-sm font-medium capitalize border-b-2 -mb-px transition-colors ${
                tab === t
                  ? 'border-stoker-orange text-stoker-orange'
                  : 'border-transparent text-gray-400 hover:text-gray-200'
              }`}
            >
              {t === 'active' ? 'Active Logs' : 'Browse Logs'}
            </button>
          ))}
        </div>

        {/* ── ACTIVE LOGS TAB ── */}
        {tab === 'active' && (
          <div className="space-y-5">
            {/* Start new log */}
            <section>
              <h4 className="text-sm font-semibold text-gray-300 mb-2">Start New Log</h4>
              <div className="flex gap-2">
                {cookerNames.length > 1 && (
                  <select
                    value={cookerName}
                    onChange={e => setCookerName(e.target.value)}
                    className="bg-stoker-dark border border-gray-600 rounded px-3 py-2 text-gray-100 text-sm"
                  >
                    {cookerNames.map(c => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                )}
                <input
                  type="text"
                  placeholder="Log name (e.g. Brisket 12h)"
                  value={logName}
                  onChange={e => setLogName(e.target.value)}
                  onKeyDown={e => { if (e.key === 'Enter') handleStart(); }}
                  className="flex-1 bg-stoker-dark border border-gray-600 rounded px-3 py-2 text-gray-100 text-sm focus:border-stoker-orange focus:outline-none"
                />
                <button
                  onClick={handleStart}
                  disabled={loading || !logName.trim()}
                  className="px-4 py-2 bg-stoker-orange text-white text-sm rounded hover:bg-orange-500 disabled:opacity-50"
                >
                  Start
                </button>
              </div>
            </section>

            {/* Active + loaded logs */}
            <section>
              <h4 className="text-sm font-semibold text-gray-300 mb-2">Active Logs</h4>
              {activeLogs.length === 0 ? (
                <p className="text-gray-500 text-sm">No active logs.</p>
              ) : (
                <div className="space-y-2">
                  {activeLogs.map(log => (
                    <div
                      key={log.logName}
                      className={`flex items-center justify-between rounded border px-3 py-2 cursor-pointer ${
                        selectedLog === log.logName
                          ? 'border-stoker-orange bg-stoker-accent/30'
                          : 'border-gray-700 hover:border-gray-500'
                      }`}
                      onClick={() => setSelectedLog(log.logName === selectedLog ? null : log.logName)}
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium text-gray-100">{log.logName}</span>
                          {log.viewOnly && (
                            <span className="text-xs bg-gray-700 text-gray-400 px-1.5 py-0.5 rounded">
                              view only
                            </span>
                          )}
                        </div>
                        <div className="text-xs text-gray-400">
                          {log.cookerName} &middot; Started {new Date(log.startTime).toLocaleString()}
                        </div>
                      </div>
                      {log.viewOnly ? (
                        <button
                          onClick={e => { e.stopPropagation(); handleCloseView(log.logName); }}
                          disabled={loading}
                          className="ml-3 px-3 py-1 text-xs bg-gray-700 hover:bg-gray-600 text-gray-200 rounded"
                        >
                          Close
                        </button>
                      ) : (
                        <button
                          onClick={e => { e.stopPropagation(); handleStop(log.logName); }}
                          disabled={loading}
                          className="ml-3 px-3 py-1 text-xs bg-gray-700 hover:bg-gray-600 text-gray-200 rounded"
                        >
                          Stop
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* Notes for selected log */}
            {selectedLog && (
              <section>
                <h4 className="text-sm font-semibold text-gray-300 mb-2">Notes — {selectedLog}</h4>
                <div className="bg-stoker-dark rounded border border-gray-700 max-h-36 overflow-y-auto mb-2">
                  {notes.length === 0 ? (
                    <p className="text-gray-500 text-xs p-3">No notes yet.</p>
                  ) : (
                    <ul className="divide-y divide-gray-700">
                      {notes.map((n, i) => (
                        <li key={i} className="px-3 py-2">
                          <span className="text-xs text-gray-400 mr-2">
                            {new Date(n.noteDate).toLocaleTimeString()}
                          </span>
                          <span className="text-sm text-gray-200">{n.note}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
                {!activeLogs.find(l => l.logName === selectedLog)?.viewOnly && (
                  <div className="flex gap-2">
                    <input
                      type="text"
                      placeholder="Add note..."
                      value={newNote}
                      onChange={e => setNewNote(e.target.value)}
                      onKeyDown={e => { if (e.key === 'Enter') handleAddNote(); }}
                      className="flex-1 bg-stoker-dark border border-gray-600 rounded px-3 py-2 text-gray-100 text-sm focus:border-stoker-orange focus:outline-none"
                    />
                    <button
                      onClick={handleAddNote}
                      disabled={!newNote.trim()}
                      className="px-4 py-2 bg-stoker-accent text-white text-sm rounded hover:bg-blue-600 disabled:opacity-50"
                    >
                      Add
                    </button>
                  </div>
                )}
              </section>
            )}
          </div>
        )}

        {/* ── BROWSE LOGS TAB ── */}
        {tab === 'browse' && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-semibold text-gray-300">Log Files</h4>
              <button
                onClick={loadBrowseTab}
                disabled={filesLoading}
                className="text-xs text-gray-400 hover:text-gray-200 disabled:opacity-50"
              >
                Refresh
              </button>
            </div>

            {filesLoading ? (
              <p className="text-gray-500 text-sm">Loading...</p>
            ) : logFiles.length === 0 ? (
              <p className="text-gray-500 text-sm">No log files found.</p>
            ) : (
              <div className="space-y-1 max-h-80 overflow-y-auto pr-1">
                {logFiles.map(file => {
                  const activeLog = activeLogs.find(l => l.logName === file.logName);
                  const alreadyLoaded = !!activeLog;
                  return (
                    <div
                      key={file.filePath}
                      className="flex items-center justify-between rounded border border-gray-700 hover:border-gray-500 px-3 py-2"
                    >
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium text-gray-100 truncate">{file.logName}</span>
                          {alreadyLoaded && (
                            <span className="text-xs bg-gray-700 text-gray-400 px-1.5 py-0.5 rounded shrink-0">
                              {activeLog.viewOnly ? 'viewing' : 'active'}
                            </span>
                          )}
                        </div>
                        <div className="text-xs text-gray-400">
                          {file.cookerName} &middot; {new Date(file.startTime).toLocaleString()} &middot; {formatSize(file.sizeBytes)}
                        </div>
                        <div className="text-xs text-gray-600 truncate">{file.relativePath}</div>
                      </div>
                      <div className="flex gap-2 ml-3 shrink-0">
                        <button
                          onClick={() => handleView(file)}
                          disabled={alreadyLoaded}
                          className="px-3 py-1 text-xs bg-stoker-accent hover:bg-blue-600 text-white rounded disabled:opacity-40 disabled:cursor-not-allowed"
                        >
                          View
                        </button>
                        <button
                          onClick={() => handleResume(file)}
                          disabled={alreadyLoaded}
                          className="px-3 py-1 text-xs bg-stoker-orange hover:bg-orange-500 text-white rounded disabled:opacity-40 disabled:cursor-not-allowed"
                        >
                          Resume
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Resume confirmation */}
      {resumeConfirm && selectedFile && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-stoker-panel border border-gray-600 rounded-xl p-6 max-w-md w-full mx-4 shadow-xl">
            <h3 className="text-base font-semibold text-gray-100 mb-2">Resume Logging?</h3>
            <p className="text-sm text-gray-300 mb-1">
              <span className="font-medium text-gray-100">{selectedFile.logName}</span>
            </p>
            <p className="text-sm text-gray-400 mb-4">
              New temperature data will be appended to this log file. Use this to continue
              a log session that was interrupted by a restart.
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => { setResumeConfirm(false); setSelectedFile(null); }}
                className="px-4 py-2 text-sm text-gray-300 hover:text-gray-100 bg-gray-700 hover:bg-gray-600 rounded"
              >
                Cancel
              </button>
              <button
                onClick={confirmResume}
                className="px-4 py-2 text-sm bg-stoker-orange hover:bg-orange-500 text-white rounded"
              >
                Resume Logging
              </button>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
