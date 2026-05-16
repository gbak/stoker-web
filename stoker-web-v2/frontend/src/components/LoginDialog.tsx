import { useState } from 'react';
import Modal from './Modal';
import { login } from '../api/stokerApi';

interface LoginDialogProps {
  onClose: () => void;
  onLoginSuccess: () => void;
}

export default function LoginDialog({ onClose, onLoginSuccess }: LoginDialogProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const ok = await login(username, password);
      if (ok) {
        onLoginSuccess();
        onClose();
      } else {
        setError('Invalid username or password.');
      }
    } catch {
      setError('Login failed. Check your connection.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal title="Sign In" onClose={onClose}>
      <form onSubmit={handleSubmit} className="p-5 space-y-4">
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 rounded px-3 py-2 text-sm">
            {error}
          </div>
        )}

        <div>
          <label className="block text-sm text-gray-400 mb-1">Username</label>
          <input
            type="text"
            autoFocus
            autoComplete="username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            className="w-full bg-stoker-dark border border-gray-600 rounded px-3 py-2 text-gray-100 focus:border-stoker-orange focus:outline-none"
          />
        </div>

        <div>
          <label className="block text-sm text-gray-400 mb-1">Password</label>
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            className="w-full bg-stoker-dark border border-gray-600 rounded px-3 py-2 text-gray-100 focus:border-stoker-orange focus:outline-none"
          />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm text-gray-400 hover:text-gray-200"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading || !username || !password}
            className="px-5 py-2 bg-stoker-orange text-white text-sm font-medium rounded hover:bg-orange-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
