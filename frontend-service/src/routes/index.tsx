import { createFileRoute } from "@tanstack/react-router";
import { useKeycloakAuth } from "../integrations/keycloak/root-provider";

export const Route = createFileRoute("/")({ component: App });

function App() {
	const {
		authenticated,
		authenticating,
		error,
		initialized,
		login,
		logout,
		profile,
		refreshToken,
		token,
	} = useKeycloakAuth();

	let statusLabel = "Checking session";
	if (initialized) {
		statusLabel = authenticated ? "Authenticated" : "Not authenticated";
	}

	let statusHint = "Bootstrapping session from Keycloak.";
	if (authenticating) {
		statusHint = "Waiting for Keycloak redirect...";
	} else if (initialized) {
		statusHint = "Session is hydrated.";
	}

	const tokenPreview =
		token && token.length > 24
			? `${token.slice(0, 12)}...${token.slice(-6)}`
			: (token ?? "Not available");

	return (
		<main className="min-h-[calc(100vh-4rem)] bg-linear-to-br from-slate-950 via-slate-900 to-black text-white px-6 py-10">
			<div className="mx-auto flex max-w-4xl flex-col gap-8">
				<div className="space-y-3">
					<p className="text-sm uppercase tracking-[0.35em] text-slate-300">
						Keycloak
					</p>
					<h1 className="text-4xl font-semibold">Sign in to Speislist</h1>
					<p className="text-lg text-slate-300">
						Use the controls below to start a Keycloak login or inspect the
						current session.
					</p>
				</div>

				<div className="flex flex-wrap gap-3">
					<button
						type="button"
						onClick={() => login()}
						disabled={!initialized || authenticating || authenticated}
						className="rounded-lg bg-cyan-600 px-4 py-2 text-sm font-semibold transition-colors hover:bg-cyan-700 disabled:cursor-not-allowed disabled:bg-gray-600"
					>
						Start login
					</button>
					<button
						type="button"
						onClick={() => logout()}
						disabled={!initialized || !authenticated}
						className="rounded-lg border border-white/10 px-4 py-2 text-sm font-semibold transition-colors hover:border-white/40 hover:bg-white/10 disabled:cursor-not-allowed disabled:border-white/5 disabled:text-white/50"
					>
						Log out
					</button>
					<button
						type="button"
						onClick={() => refreshToken()}
						disabled={!authenticated}
						className="rounded-lg border border-cyan-400/30 px-4 py-2 text-sm font-semibold text-cyan-100 transition-colors hover:border-cyan-300 hover:text-white disabled:cursor-not-allowed disabled:border-white/10 disabled:text-white/50"
					>
						Refresh token
					</button>
				</div>

				{error && (
					<div className="rounded-lg border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">
						{error}
					</div>
				)}

				<div className="grid gap-4 md:grid-cols-3">
					<div className="rounded-xl border border-white/10 bg-white/5 p-5 shadow-lg">
						<p className="text-sm text-slate-300">Status</p>
						<p className="text-2xl font-semibold">{statusLabel}</p>
						<p className="text-xs text-slate-400">{statusHint}</p>
					</div>
					<div className="rounded-xl border border-white/10 bg-white/5 p-5 shadow-lg">
						<p className="text-sm text-slate-300">Profile</p>
						<dl className="mt-2 space-y-1 text-sm text-slate-200">
							<div className="flex justify-between">
								<dt className="text-slate-400">Username</dt>
								<dd>{profile?.username ?? "Not set"}</dd>
							</div>
							<div className="flex justify-between">
								<dt className="text-slate-400">Name</dt>
								<dd>
									{profile?.firstName || profile?.lastName
										? `${profile?.firstName ?? ""} ${profile?.lastName ?? ""}`.trim()
										: "Not set"}
								</dd>
							</div>
							<div className="flex justify-between">
								<dt className="text-slate-400">Email</dt>
								<dd>{profile?.email ?? "Not set"}</dd>
							</div>
						</dl>
					</div>
					<div className="rounded-xl border border-white/10 bg-white/5 p-5 shadow-lg">
						<p className="text-sm text-slate-300">Token</p>
						<p className="break-all font-mono text-sm text-lime-200">
							{tokenPreview}
						</p>
						<p className="mt-2 text-xs text-slate-400">
							Tokens refresh automatically when they expire. Use Refresh token
							to force a manual update.
						</p>
					</div>
				</div>
			</div>
		</main>
	);
}
