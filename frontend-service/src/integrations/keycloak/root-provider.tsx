import Keycloak, {
	type KeycloakInitOptions,
	type KeycloakLoginOptions,
	type KeycloakLogoutOptions,
	type KeycloakProfile,
} from "keycloak-js";
import {
	createContext,
	type PropsWithChildren,
	useCallback,
	useContext,
	useEffect,
	useMemo,
	useState,
} from "react";

const keycloakConfig = {
	url: "http://localhost:8081",
	realm: "speislist",
	clientId: "frontend-service",
};

const formatError = (error: unknown) =>
	error instanceof Error ? error.message : "Keycloak request failed";

type KeycloakClient = InstanceType<typeof Keycloak> | null;

let keycloakSingleton: KeycloakClient = null;

const createKeycloakClient = (): KeycloakClient => {
	if (globalThis.window === undefined) {
		return null;
	}

	keycloakSingleton ??= new Keycloak(keycloakConfig);

	return keycloakSingleton;
};

export type KeycloakRouterContext = {
	keycloak: KeycloakClient;
};

export function getContext(): KeycloakRouterContext {
	return {
		keycloak: createKeycloakClient(),
	};
}

type AuthState = {
	initialized: boolean;
	authenticating: boolean;
	authenticated: boolean;
	profile: KeycloakProfile | null;
	token?: string;
	error: string | null;
};

type KeycloakAuthContextValue = AuthState & {
	keycloak: KeycloakClient;
	login: (options?: KeycloakLoginOptions) => Promise<void>;
	logout: (options?: KeycloakLogoutOptions) => Promise<void>;
	refreshToken: (minValidity?: number) => Promise<boolean>;
};

const KeycloakAuthContext = createContext<KeycloakAuthContextValue | undefined>(
	undefined,
);

export function Provider({
	children,
	keycloak,
}: PropsWithChildren<KeycloakRouterContext>) {
	const [state, setState] = useState<AuthState>({
		authenticated: false,
		authenticating: false,
		error: null,
		initialized: false,
		profile: null,
		token: undefined,
	});

	const refreshToken = useCallback(
		async (minValidity = 30) => {
			if (!keycloak) {
				return false;
			}

			const refreshed = await keycloak.updateToken(minValidity);

			setState((prev) => ({
				...prev,
				authenticated: keycloak.authenticated ?? false,
				token: keycloak.token ?? undefined,
			}));

			return refreshed;
		},
		[keycloak],
	);

	const login = useCallback(
		async (options?: KeycloakLoginOptions) => {
			if (!keycloak) {
				throw new Error("Keycloak is not available in this environment");
			}

			setState((prev) => ({ ...prev, authenticating: true, error: null }));

			await keycloak.login({
				redirectUri: globalThis.location.href,
				...options,
			});
		},
		[keycloak],
	);

	const logout = useCallback(
		async (options?: KeycloakLogoutOptions) => {
			if (!keycloak) {
				setState({
					authenticated: false,
					authenticating: false,
					error: null,
					initialized: true,
					profile: null,
					token: undefined,
				});
				return;
			}

			await keycloak.logout({
				redirectUri: globalThis.location.origin,
				...options,
			});

			setState({
				authenticated: false,
				authenticating: false,
				error: null,
				initialized: true,
				profile: null,
				token: undefined,
			});
		},
		[keycloak],
	);

	useEffect(() => {
		if (!keycloak) {
			setState((prev) => ({ ...prev, initialized: true }));
			return;
		}

		let cancelled = false;

		const initOptions: KeycloakInitOptions = {
			checkLoginIframe: false,
			onLoad: "check-sso",
			pkceMethod: "S256",
		};

		const reportError = (error: unknown) => {
			if (!cancelled) {
				setState((prev) => ({ ...prev, error: formatError(error) }));
			}
		};

		const initialize = async () => {
			setState((prev) => ({ ...prev, authenticating: true, error: null }));

			try {
				const authenticated = await keycloak.init(initOptions);

				if (cancelled) {
					return;
				}

				setState((prev) => ({
					...prev,
					authenticated,
					authenticating: false,
					initialized: true,
					token: keycloak.token ?? undefined,
				}));

				if (authenticated) {
					try {
						const profile = await keycloak.loadUserProfile();

						if (!cancelled) {
							setState((prev) => ({ ...prev, profile }));
						}
					} catch (profileError: unknown) {
						if (!cancelled) {
							setState((prev) => ({
								...prev,
								error: formatError(profileError),
							}));
						}
					}
				}
			} catch (initError: unknown) {
				if (cancelled) {
					return;
				}

				setState((prev) => ({
					...prev,
					authenticated: false,
					authenticating: false,
					error: formatError(initError),
					initialized: true,
					profile: null,
					token: undefined,
				}));
			}
		};

		void initialize();

		keycloak.onTokenExpired = () => {
			void refreshToken().catch(reportError);
		};

		keycloak.onAuthLogout = () => {
			if (!cancelled) {
				setState({
					authenticated: false,
					authenticating: false,
					error: null,
					initialized: true,
					profile: null,
					token: undefined,
				});
			}
		};

		return () => {
			cancelled = true;
		};
	}, [keycloak, refreshToken]);

	const value = useMemo(
		() => ({
			...state,
			keycloak,
			login,
			logout,
			refreshToken,
		}),
		[keycloak, login, logout, refreshToken, state],
	);

	return (
		<KeycloakAuthContext.Provider value={value}>
			{children}
		</KeycloakAuthContext.Provider>
	);
}

export function useKeycloakAuth() {
	const context = useContext(KeycloakAuthContext);

	if (!context) {
		throw new Error("useKeycloakAuth must be used inside Keycloak.Provider");
	}

	return context;
}
