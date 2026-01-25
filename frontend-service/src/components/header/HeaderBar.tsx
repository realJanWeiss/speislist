import { Link } from "@tanstack/react-router";
import { Menu } from "lucide-react";

export type HeaderBarProps = {
	actionError: string | null;
	authButtonLabel: string;
	authDisabled: boolean;
	error: string | null;
	onAuthClick: () => void;
	onMenuClick: () => void;
	subStatusLabel: string;
	statusLabel: string;
};

export default function HeaderBar({
	actionError,
	authButtonLabel,
	authDisabled,
	error,
	onAuthClick,
	onMenuClick,
	subStatusLabel,
	statusLabel,
}: Readonly<HeaderBarProps>) {
	return (
		<header className="p-4 flex items-center bg-gray-800 text-white shadow-lg">
			<button
				type="button"
				onClick={onMenuClick}
				className="p-2 hover:bg-gray-700 rounded-lg transition-colors"
				aria-label="Open menu"
			>
				<Menu size={24} />
			</button>

			<Link to="/" className="ml-4 text-xl font-semibold">
				Speislist
			</Link>

			<div className="ml-auto flex items-center gap-3">
				<div className="flex flex-col text-right leading-tight">
					<span className="text-sm font-semibold text-white">
						{statusLabel}
					</span>
					<span className="text-xs text-cyan-300">{subStatusLabel}</span>
					{error && <span className="text-[11px] text-red-300">{error}</span>}
					{actionError && (
						<span className="text-[11px] text-red-300">{actionError}</span>
					)}
				</div>
				<button
					type="button"
					onClick={onAuthClick}
					disabled={authDisabled}
					className="px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-700 disabled:bg-gray-600 disabled:cursor-not-allowed transition-colors text-sm font-semibold"
				>
					{authButtonLabel}
				</button>
			</div>
		</header>
	);
}
