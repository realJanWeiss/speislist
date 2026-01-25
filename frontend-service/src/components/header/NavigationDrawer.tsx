import { Link } from "@tanstack/react-router";
import {
	ChevronDown,
	ChevronRight,
	ClipboardType,
	Home,
	Network,
	SquareFunction,
	StickyNote,
	X,
} from "lucide-react";

export type NavigationDrawerProps = {
	groupedExpanded: Record<string, boolean>;
	isOpen: boolean;
	onClose: () => void;
	onToggleGroup: (groupKey: string) => void;
};

export default function NavigationDrawer({
	groupedExpanded,
	isOpen,
	onClose,
	onToggleGroup,
}: Readonly<NavigationDrawerProps>) {
	const handleGroupToggle = (groupKey: string) => {
		onToggleGroup(groupKey);
	};

	const handleNavigate = () => {
		onClose();
	};

	return (
		<aside
			className={`fixed top-0 left-0 h-full w-80 bg-gray-900 text-white shadow-2xl z-50 transform transition-transform duration-300 ease-in-out flex flex-col ${
				isOpen ? "translate-x-0" : "-translate-x-full"
			}`}
		>
			<div className="flex items-center justify-between p-4 border-b border-gray-700">
				<h2 className="text-xl font-bold">Navigation</h2>
				<button
					type="button"
					onClick={onClose}
					className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
					aria-label="Close menu"
				>
					<X size={24} />
				</button>
			</div>

			<nav className="flex-1 p-4 overflow-y-auto">
				<Link
					to="/"
					onClick={handleNavigate}
					className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
					activeProps={{
						className:
							"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
					}}
				>
					<Home size={20} />
					<span className="font-medium">Home</span>
				</Link>

				<Link
					to="/demo/start/server-funcs"
					onClick={handleNavigate}
					className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
					activeProps={{
						className:
							"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
					}}
				>
					<SquareFunction size={20} />
					<span className="font-medium">Start - Server Functions</span>
				</Link>

				<Link
					to="/demo/start/api-request"
					onClick={handleNavigate}
					className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
					activeProps={{
						className:
							"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
					}}
				>
					<Network size={20} />
					<span className="font-medium">Start - API Request</span>
				</Link>

				<div className="flex flex-row justify-between">
					<Link
						to="/demo/start/ssr"
						onClick={handleNavigate}
						className="flex-1 flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
						activeProps={{
							className:
								"flex-1 flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
						}}
					>
						<StickyNote size={20} />
						<span className="font-medium">Start - SSR Demos</span>
					</Link>
					<button
						type="button"
						className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
						onClick={() => handleGroupToggle("StartSSRDemo")}
					>
						{groupedExpanded.StartSSRDemo ? (
							<ChevronDown size={20} />
						) : (
							<ChevronRight size={20} />
						)}
					</button>
				</div>
				{groupedExpanded.StartSSRDemo && (
					<div className="flex flex-col ml-4">
						<Link
							to="/demo/start/ssr/spa-mode"
							onClick={handleNavigate}
							className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
							activeProps={{
								className:
									"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
							}}
						>
							<StickyNote size={20} />
							<span className="font-medium">SPA Mode</span>
						</Link>

						<Link
							to="/demo/start/ssr/full-ssr"
							onClick={handleNavigate}
							className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
							activeProps={{
								className:
									"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
							}}
						>
							<StickyNote size={20} />
							<span className="font-medium">Full SSR</span>
						</Link>

						<Link
							to="/demo/start/ssr/data-only"
							onClick={handleNavigate}
							className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
							activeProps={{
								className:
									"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
							}}
						>
							<StickyNote size={20} />
							<span className="font-medium">Data Only</span>
						</Link>
					</div>
				)}

				<Link
					to="/demo/form/simple"
					onClick={handleNavigate}
					className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
					activeProps={{
						className:
							"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
					}}
				>
					<ClipboardType size={20} />
					<span className="font-medium">Simple Form</span>
				</Link>

				<Link
					to="/demo/form/address"
					onClick={handleNavigate}
					className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
					activeProps={{
						className:
							"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
					}}
				>
					<ClipboardType size={20} />
					<span className="font-medium">Address Form</span>
				</Link>

				<Link
					to="/demo/tanstack-query"
					onClick={handleNavigate}
					className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition-colors mb-2"
					activeProps={{
						className:
							"flex items-center gap-3 p-3 rounded-lg bg-cyan-600 hover:bg-cyan-700 transition-colors mb-2",
					}}
				>
					<Network size={20} />
					<span className="font-medium">TanStack Query</span>
				</Link>
			</nav>
		</aside>
	);
}
