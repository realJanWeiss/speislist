import { ShoppingList } from "@/data/types";
import { useEffect, useRef } from "react";

export default function ShoppingListModal({
    list,
    onClose,
}: Readonly<{
    list: ShoppingList;
    onClose: () => void;
}>) {
    const dialogRef = useRef<HTMLDialogElement>(null);
    const listName = list.name ?? "Untitled list";
    const items = list.items ?? [];
    const members = list.members ?? [];

    useEffect(() => {
        const dialog = dialogRef.current;
        if (dialog && !dialog.open) {
            dialog.showModal();
        }

        const handleCancel = (e: Event) => {
            e.preventDefault();
            onClose();
        };

        const handleBackdropClick = (e: MouseEvent) => {
            const rect = dialog?.getBoundingClientRect();
            if (
                rect &&
                (e.clientX < rect.left ||
                    e.clientX > rect.right ||
                    e.clientY < rect.top ||
                    e.clientY > rect.bottom)
            ) {
                onClose();
            }
        };

        dialog?.addEventListener("cancel", handleCancel);
        dialog?.addEventListener("click", handleBackdropClick);
        return () => {
            dialog?.removeEventListener("cancel", handleCancel);
            dialog?.removeEventListener("click", handleBackdropClick);
        };
    }, [onClose]);

    return (
        <dialog
            ref={dialogRef}
            className="fixed inset-0 z-50 m-auto max-h-[85vh] w-full max-w-lg overflow-hidden rounded-2xl border border-white/20 bg-slate-900 p-0 shadow-2xl backdrop:bg-black/70 backdrop:backdrop-blur-sm"
        >
            {/* Header */}
            <div className="flex items-center justify-between border-b border-white/10 px-6 py-4">
                <h2 className="text-2xl font-semibold text-white">{listName}</h2>
                <button
                    type="button"
                    onClick={onClose}
                    className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-white/10 hover:text-white"
                    aria-label="Close modal"
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        strokeWidth={2}
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            d="M6 18L18 6M6 6l12 12"
                        />
                    </svg>
                </button>
            </div>

            {/* Content */}
            <div className="max-h-[60vh] overflow-y-auto px-6 py-4">
                {/* Items Section */}
                <section className="mb-6">
                    <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wider text-slate-400">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-4 w-4"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            strokeWidth={2}
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                            />
                        </svg>
                        Items ({items.length})
                    </h3>
                    {items.length === 0 ? (
                        <p className="text-sm text-slate-500">No items in this list</p>
                    ) : (
                        <ul className="space-y-2">
                            {items.map((item) => (
                                <li
                                    key={item.id}
                                    className={`flex items-center justify-between rounded-lg border px-4 py-3 ${item.isCompleted
                                        ? "border-green-500/30 bg-green-500/10"
                                        : "border-white/10 bg-white/5"
                                        }`}
                                >
                                    <div className="flex items-center gap-3">
                                        <span
                                            className={`flex h-5 w-5 items-center justify-center rounded-full border ${item.isCompleted
                                                ? "border-green-500 bg-green-500 text-white"
                                                : "border-slate-500"
                                                }`}
                                        >
                                            {item.isCompleted && (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    className="h-3 w-3"
                                                    fill="none"
                                                    viewBox="0 0 24 24"
                                                    stroke="currentColor"
                                                    strokeWidth={3}
                                                >
                                                    <path
                                                        strokeLinecap="round"
                                                        strokeLinejoin="round"
                                                        d="M5 13l4 4L19 7"
                                                    />
                                                </svg>
                                            )}
                                        </span>
                                        <span
                                            className={
                                                item.isCompleted
                                                    ? "text-slate-400 line-through"
                                                    : "text-white"
                                            }
                                        >
                                            {item.name}
                                        </span>
                                    </div>
                                    <span className="rounded-full bg-slate-700 px-2 py-0.5 text-xs text-slate-300">
                                        ×{item.quantity}
                                    </span>
                                </li>
                            ))}
                        </ul>
                    )}
                </section>

                {/* Members Section */}
                <section>
                    <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wider text-slate-400">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-4 w-4"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            strokeWidth={2}
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                            />
                        </svg>
                        Members ({members.length})
                    </h3>
                    {members.length === 0 ? (
                        <p className="text-sm text-slate-500">No members</p>
                    ) : (
                        <ul className="flex flex-wrap gap-2">
                            {members.map((member) => (
                                <li
                                    key={member.id}
                                    className="flex items-center gap-2 rounded-full border border-cyan-500/30 bg-cyan-500/10 px-3 py-1.5"
                                >
                                    <span className="flex h-6 w-6 items-center justify-center rounded-full bg-cyan-600 text-xs font-semibold text-white">
                                        {member.userName.charAt(0).toUpperCase()}
                                    </span>
                                    <span className="text-sm text-cyan-100">
                                        {member.userName}
                                    </span>
                                </li>
                            ))}
                        </ul>
                    )}
                </section>
            </div>

            {/* Footer */}
            <div className="border-t border-white/10 px-6 py-4">
                <p className="text-xs text-slate-500">
                    Created {formatTimestamp(list.createdAt)}
                </p>
            </div>
        </dialog>
    );
}

function formatTimestamp(value?: string): string {
    if (!value) {
        return "recently";
    }

    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
        return "recently";
    }

    return parsed.toLocaleString();
}