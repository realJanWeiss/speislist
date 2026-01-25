import { useState } from "react";
import { useKeycloakAuth } from "../../integrations/keycloak/root-provider";
import HeaderBar from "./HeaderBar";
import NavigationDrawer from "./NavigationDrawer";

export default function Header() {
	const [isOpen, setIsOpen] = useState(false);
	const [groupedExpanded, setGroupedExpanded] = useState<
		Record<string, boolean>
	>({});

	const {
		authenticated,
		authenticating,
		error,
		initialized,
		login,
		logout,
		profile,
	} = useKeycloakAuth();
	const [actionError, setActionError] = useState<string | null>(null);

	const statusLabel = authenticated
		? profile?.firstName || profile?.username || "Authenticated"
		: "Guest";

	let subStatusLabel = "Checking session...";
	if (initialized) {
		subStatusLabel = authenticated ? "Signed in" : "Signed out";
	}

	let authButtonLabel = "Log in";
	if (authenticating) {
		authButtonLabel = "Redirecting...";
	} else if (authenticated) {
		authButtonLabel = "Log out";
	}

	const handleAuthClick = async () => {
		setActionError(null);

		try {
			if (authenticated) {
				await logout();
			} else {
				await login();
			}
		} catch (authError) {
			const fallbackMessage =
				authError instanceof Error
					? authError.message
					: "Authentication request failed";
			setActionError(fallbackMessage);
		}
	};

	const toggleGroup = (groupKey: string) => {
		setGroupedExpanded((prev) => ({
			...prev,
			[groupKey]: !prev[groupKey],
		}));
	};

	return (
		<>
			<HeaderBar
				actionError={actionError}
				authButtonLabel={authButtonLabel}
				authDisabled={!initialized || authenticating}
				error={error}
				onAuthClick={handleAuthClick}
				onMenuClick={() => setIsOpen(true)}
				subStatusLabel={subStatusLabel}
				statusLabel={statusLabel}
			/>
			<NavigationDrawer
				groupedExpanded={groupedExpanded}
				isOpen={isOpen}
				onClose={() => setIsOpen(false)}
				onToggleGroup={toggleGroup}
			/>
		</>
	);
}
