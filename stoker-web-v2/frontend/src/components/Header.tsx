interface HeaderProps {
  connected: boolean;
  authenticated: boolean;
  onLogin: () => void;
  onLogout: () => void;
  onLogs: () => void;
  onAlerts: () => void;
  onConfig: () => void;
}

export default function Header({
  connected,
  authenticated,
  onLogin,
  onLogout,
  onLogs,
  onAlerts,
  onConfig,
}: HeaderProps) {
  return (
    <header className="bg-stoker-panel border-b border-gray-700 px-4 py-3 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <h1 className="text-xl font-bold text-stoker-orange tracking-wide">Stoker-Web</h1>
      </div>

      <nav className="flex items-center gap-1">
        {authenticated && (
          <>
            <NavButton onClick={onLogs}>Logs</NavButton>
            <NavButton onClick={onAlerts}>Alerts</NavButton>
            <NavButton onClick={onConfig}>Configure</NavButton>
          </>
        )}

        <div className="ml-3 flex items-center gap-2 text-sm border-l border-gray-700 pl-3">
          <span
            className={`inline-block w-2.5 h-2.5 rounded-full ${connected ? 'bg-green-400' : 'bg-red-500'}`}
          />
          <span className={connected ? 'text-green-400' : 'text-red-400'}>
            {connected ? 'Connected' : 'Disconnected'}
          </span>

          {authenticated ? (
            <button
              onClick={onLogout}
              className="ml-2 text-xs text-gray-400 hover:text-gray-200 border border-gray-600 rounded px-2 py-1"
            >
              Sign out
            </button>
          ) : (
            <button
              onClick={onLogin}
              className="ml-2 text-xs text-stoker-orange hover:text-orange-400 border border-stoker-orange/50 rounded px-2 py-1"
            >
              Sign in
            </button>
          )}
        </div>
      </nav>
    </header>
  );
}

function NavButton({ onClick, children }: { onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      className="px-3 py-1.5 text-sm text-gray-300 hover:text-white hover:bg-stoker-accent/40 rounded transition-colors"
    >
      {children}
    </button>
  );
}
