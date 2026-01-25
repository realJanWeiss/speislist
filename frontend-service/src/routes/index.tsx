import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import type React from "react";
import { useKeycloakAuth } from "../integrations/keycloak/root-provider";

type ShoppingList = {
	createdAt?: string;
	description?: string;
	id: string;
	items?: Array<{ name?: string }> | null;
	name?: string;
	title?: string;
	updatedAt?: string;
};

const SHOPPING_LISTS_ENDPOINT = "http://localhost:8080/api/shopping-lists";

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

	const canFetchLists = authenticated && Boolean(token);
	const listsQuery = useQuery<ShoppingList[]>({
		queryKey: ["shopping-lists", token],
		queryFn: () => {
			if (!token) {
				throw new Error("Missing access token");
			}
			return fetchShoppingLists(token);
		},
		enabled: canFetchLists,
		staleTime: 30_000,
	});

	if (!initialized || authenticating) {
		return <LoadingView />;
	}

	if (!authenticated) {
		return <LoggedOutView onLogin={login} errorMessage={error} />;
	}

	if (!token) {
		return <MissingTokenView onRefresh={refreshToken} onLogout={logout} />;
	}

	return (
		<ShoppingListsView
			lists={listsQuery.data ?? []}
			isLoading={listsQuery.isLoading}
			isRefreshing={listsQuery.isFetching}
			errorMessage={
				listsQuery.isError
					? (listsQuery.error?.message ?? "Unable to load lists")
					: null
			}
			onRefresh={() => listsQuery.refetch()}
			onLogout={logout}
			profileName={getProfileName(profile)}
			keycloakError={error}
		/>
	);
}

function LoadingView() {
	return (
		<main className="min-h-[calc(100vh-4rem)] bg-linear-to-br from-gray-900 via-slate-900 to-black text-white px-6 py-20">
			<div className="mx-auto max-w-3xl text-center space-y-4">
				<p className="text-sm uppercase tracking-[0.4em] text-slate-400">
					Speislist
				</p>
				<h1 className="text-4xl font-semibold">Preparing your workspace</h1>
				<p className="text-slate-300">
					Hold tight while we verify your session with Keycloak.
				</p>
				<div className="flex justify-center">
					<span className="h-1 w-40 animate-pulse rounded-full bg-cyan-500" />
				</div>
			</div>
		</main>
	);
}

function LoggedOutView({
	onLogin,
	errorMessage,
}: Readonly<{
	onLogin: () => Promise<void>;
	errorMessage: string | null;
}>) {
	return (
		<main className="min-h-[calc(100vh-4rem)] bg-linear-to-br from-slate-950 via-slate-900 to-black text-white px-6 py-16">
			<div className="mx-auto max-w-2xl space-y-6 text-center">
				<p className="text-sm uppercase tracking-[0.35em] text-slate-400">
					Speislist
				</p>
				<h1 className="text-4xl font-semibold">Sign in to manage your lists</h1>
				<p className="text-lg text-slate-300">
					Connect with Keycloak to sync your shopping lists across devices.
				</p>
				<button
					type="button"
					onClick={() => onLogin()}
					className="rounded-lg bg-cyan-600 px-6 py-3 text-base font-semibold transition-colors hover:bg-cyan-700 disabled:cursor-not-allowed disabled:bg-gray-600"
				>
					Continue with Keycloak
				</button>
				{errorMessage && <p className="text-sm text-red-300">{errorMessage}</p>}
			</div>
		</main>
	);
}

function MissingTokenView({
	onRefresh,
	onLogout,
}: Readonly<{
	onLogout: () => Promise<void>;
	onRefresh: () => Promise<boolean>;
}>) {
	return (
		<main className="min-h-[calc(100vh-4rem)] bg-linear-to-br from-slate-950 via-slate-900 to-black text-white px-6 py-16">
			<div className="mx-auto max-w-2xl space-y-6 text-center">
				<h1 className="text-3xl font-semibold">We lost your session token</h1>
				<p className="text-slate-300">
					Refresh your token or sign out and sign back in to continue.
				</p>
				<div className="flex flex-wrap justify-center gap-3">
					<button
						type="button"
						onClick={() => onRefresh()}
						className="rounded-lg border border-cyan-500 px-4 py-2 text-sm font-semibold text-cyan-100 transition-colors hover:bg-cyan-600/10"
					>
						Refresh token
					</button>
					<button
						type="button"
						onClick={() => onLogout()}
						className="rounded-lg border border-white/20 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-white/10"
					>
						Log out
					</button>
				</div>
			</div>
		</main>
	);
}

function ShoppingListsView({
	errorMessage,
	isLoading,
	isRefreshing,
	lists,
	onLogout,
	onRefresh,
	profileName,
	keycloakError,
}: Readonly<{
	errorMessage: string | null;
	isLoading: boolean;
	isRefreshing: boolean;
	lists: ShoppingList[];
	onLogout: () => Promise<void>;
	onRefresh: () => void;
	profileName: string;
	keycloakError: string | null;
}>) {
	const hasLists = lists.length > 0;
	let listContent: React.ReactNode = null;

	if (isLoading) {
		listContent = <ListSkeleton />;
	} else if (hasLists) {
		listContent = <ListGrid lists={lists} />;
	} else {
		listContent = <EmptyState />;
	}

	return (
		<main className="min-h-[calc(100vh-4rem)] bg-linear-to-br from-slate-950 via-slate-900 to-black text-white px-6 py-10">
			<div className="mx-auto flex max-w-5xl flex-col gap-8">
				<header className="space-y-2">
					<p className="text-sm uppercase tracking-[0.3em] text-slate-400">
						Your Lists
					</p>
					<h1 className="text-4xl font-semibold">Hey {profileName}!</h1>
					<p className="text-lg text-slate-300">
						Manage every grocery plan from one place.
					</p>
				</header>

				<div className="flex flex-wrap gap-3">
					<button
						type="button"
						onClick={onRefresh}
						disabled={isRefreshing}
						className="rounded-lg border border-cyan-500/40 px-4 py-2 text-sm font-semibold text-cyan-100 transition-colors hover:border-cyan-400 hover:text-white disabled:cursor-not-allowed disabled:border-white/10"
					>
						{isRefreshing ? "Refreshing..." : "Refresh lists"}
					</button>
					<button
						type="button"
						onClick={() => onLogout()}
						className="rounded-lg border border-white/10 px-4 py-2 text-sm font-semibold transition-colors hover:border-white/40 hover:bg-white/10"
					>
						Log out
					</button>
				</div>

				{keycloakError && (
					<p className="text-sm text-amber-200">
						Session warning: {keycloakError}
					</p>
				)}

				{errorMessage && (
					<div className="rounded-lg border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">
						{errorMessage}
					</div>
				)}

				{listContent}
			</div>
		</main>
	);
}

function ListSkeleton() {
	return (
		<div className="grid gap-4 md:grid-cols-2">
			{Array.from({ length: 4 }).map((_, index) => (
				<div
					className="rounded-xl border border-white/5 bg-white/5 p-5"
					key={`list-skeleton-${index.toString()}`}
				>
					<div className="h-5 w-32 animate-pulse rounded bg-white/20" />
					<div className="mt-3 h-4 w-full animate-pulse rounded bg-white/10" />
					<div className="mt-2 h-4 w-3/4 animate-pulse rounded bg-white/10" />
				</div>
			))}
		</div>
	);
}

function ListGrid({ lists }: Readonly<{ lists: ShoppingList[] }>) {
	return (
		<div className="grid gap-4 md:grid-cols-2">
			{lists.map((list) => (
				<article
					className="rounded-xl border border-white/15 bg-white/5 p-5 shadow-lg"
					key={list.id}
				>
					<header className="flex items-center justify-between">
						<div>
							<h2 className="text-xl font-semibold">
								{list.name ?? list.title ?? "Untitled list"}
							</h2>
							<p className="text-sm text-slate-300">
								Updated {formatTimestamp(list.updatedAt ?? list.createdAt)}
							</p>
						</div>
						<span className="rounded-full border border-cyan-500/40 px-3 py-1 text-xs text-cyan-100">
							{formatItemCount(list.items)} items
						</span>
					</header>
					{list.description && (
						<p className="mt-3 text-sm text-slate-200">{list.description}</p>
					)}
				</article>
			))}
		</div>
	);
}

function EmptyState() {
	return (
		<div className="rounded-2xl border border-dashed border-white/20 bg-white/5 p-10 text-center">
			<h2 className="text-2xl font-semibold">No shopping lists yet</h2>
			<p className="mt-2 text-slate-300">
				Create your first list in the backend service and it will appear here.
			</p>
		</div>
	);
}

async function fetchShoppingLists(token: string): Promise<ShoppingList[]> {
	const response = await fetch(SHOPPING_LISTS_ENDPOINT, {
		headers: {
			Authorization: `Bearer ${token}`,
			"Content-Type": "application/json",
		},
	});

	if (!response.ok) {
		throw new Error(
			`Unable to load shopping lists (status ${response.status.toString()})`,
		);
	}

	const payload = await response.json();
	return Array.isArray(payload) ? payload : [];
}

function formatTimestamp(value?: string) {
	if (!value) {
		return "recently";
	}

	const parsed = new Date(value);
	if (Number.isNaN(parsed.getTime())) {
		return "recently";
	}

	return parsed.toLocaleString();
}

function formatItemCount(items?: Array<{ name?: string }> | null) {
	if (!Array.isArray(items)) {
		return 0;
	}
	return items.length;
}

function getProfileName(
	profile: {
		firstName?: string | null;
		lastName?: string | null;
		username?: string;
	} | null,
) {
	if (!profile) {
		return "there";
	}

	const friendly =
		`${profile.firstName ?? ""} ${profile.lastName ?? ""}`.trim();
	return friendly || profile.username || "there";
}
