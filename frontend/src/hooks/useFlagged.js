const KEY = "dh_flagged";

function load() {
    try { return JSON.parse(localStorage.getItem(KEY)) ?? []; }
    catch { return []; }
}

export function getFlagged() {
    return load();
}

export function flagContent({ targetType, targetId, title, reason }) {
    const existing = load();
    if (existing.some((f) => f.targetType === targetType && f.targetId === targetId)) return false;
    const updated = [
        {
            id: Date.now(),
            targetType,   
            targetId,
            title,
            reason,
            status: "PENDING",
            flaggedAt: new Date().toISOString(),
        },
        ...existing,
    ];
    localStorage.setItem(KEY, JSON.stringify(updated));
    return true;
}

export function updateFlagStatus(id, status) {
    const updated = load().map((f) => f.id === id ? { ...f, status } : f);
    localStorage.setItem(KEY, JSON.stringify(updated));
    return updated;
}
