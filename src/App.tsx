// src/App.tsx
// Nyro Browser — SolidJS + TypeScript + Tailwind CSS

import {
  Component,
  createSignal,
  createEffect,
  For,
  Show,
  onMount,
} from "solid-js";
import { invoke } from "@tauri-apps/api/tauri";

// ─── Types ────────────────────────────────────────────────────────────────────

interface Tab {
  id: number;
  title: string;
  url: string;
  favicon: string;
  isLoading: boolean;
}

let tabIdCounter = 1;

function createTab(url = "https://www.google.com", title = "New Tab"): Tab {
  return {
    id: tabIdCounter++,
    title,
    url,
    favicon: "🌐",
    isLoading: false,
  };
}

// ─── Utility ──────────────────────────────────────────────────────────────────

function getDomain(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch {
    return url;
  }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

const TabItem: Component<{
  tab: Tab;
  isActive: boolean;
  onActivate: () => void;
  onClose: () => void;
}> = (props) => {
  return (
    <div
      class={`tab-item group relative flex items-center min-w-[180px] max-w-[240px] h-[34px] px-3 cursor-pointer select-none flex-shrink-0 ${
        props.isActive ? "tab-active" : "tab-inactive"
      }`}
      onClick={props.onActivate}
    >
      {/* Trapezoidal SVG shape background */}
      <svg
        class="tab-shape absolute inset-0 w-full h-full"
        viewBox="0 0 240 34"
        preserveAspectRatio="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d={`M0,34 L8,0 L232,0 L240,34 Z`}
          fill={props.isActive ? "#ffffff" : "transparent"}
          class={props.isActive ? "" : "group-hover:fill-[#e8eaed]"}
          style={{ transition: "fill 0.15s ease" }}
        />
      </svg>

      {/* Tab content */}
      <div class="relative z-10 flex items-center gap-2 w-full min-w-0">
        {/* Favicon */}
        <span class="text-xs flex-shrink-0 w-4 text-center">
          {props.tab.isLoading ? (
            <span class="loading-spinner inline-block w-3 h-3 border border-[#5f6368] border-t-[#1a73e8] rounded-full" />
          ) : (
            props.tab.favicon
          )}
        </span>

        {/* Title */}
        <span
          class={`text-[13px] truncate flex-1 leading-none ${
            props.isActive ? "text-[#202124]" : "text-[#3c4043]"
          }`}
        >
          {props.tab.title}
        </span>

        {/* Close button */}
        <button
          class="flex-shrink-0 w-4 h-4 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-[#dadce0] transition-all"
          onClick={(e) => {
            e.stopPropagation();
            props.onClose();
          }}
          title="Close tab"
        >
          <svg viewBox="0 0 16 16" class="w-3 h-3 fill-[#5f6368]">
            <path d="M4 4l8 8M12 4l-8 8" stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" />
          </svg>
        </button>
      </div>
    </div>
  );
};

// ─── Main App ─────────────────────────────────────────────────────────────────

const App: Component = () => {
  const [tabs, setTabs] = createSignal<Tab[]>([createTab()]);
  const [activeTabId, setActiveTabId] = createSignal<number>(tabs()[0].id);
  const [omniboxValue, setOmniboxValue] = createSignal("");
  const [omniboxFocused, setOmniboxFocused] = createSignal(false);
  const [isMaximized, setIsMaximized] = createSignal(false);

  let omniboxRef: HTMLInputElement | undefined;

  // ── Derived active tab ──
  const activeTab = () => tabs().find((t) => t.id === activeTabId())!;

  // ── Sync omnibox with active tab URL ──
  createEffect(() => {
    const tab = activeTab();
    if (tab && !omniboxFocused()) {
      setOmniboxValue(tab.url);
    }
  });

  // ── Tab management ──────────────────────────────────────────────────────────

  function addTab() {
    const newTab = createTab();
    setTabs((prev) => [...prev, newTab]);
    setActiveTabId(newTab.id);
  }

  function closeTab(id: number) {
    const list = tabs();
    if (list.length === 1) {
      // Last tab: reset to new tab instead of closing
      setTabs([createTab()]);
      setActiveTabId(tabs()[0].id);
      return;
    }
    const idx = list.findIndex((t) => t.id === id);
    const newList = list.filter((t) => t.id !== id);
    setTabs(newList);
    if (activeTabId() === id) {
      const nextIdx = Math.min(idx, newList.length - 1);
      setActiveTabId(newList[nextIdx].id);
    }
  }

  function updateTab(id: number, patch: Partial<Tab>) {
    setTabs((prev) => prev.map((t) => (t.id === id ? { ...t, ...patch } : t)));
  }

  // ── Navigation ───────────────────────────────────────────────────────────────

  async function navigate(rawInput: string) {
    const tab = activeTab();
    if (!tab) return;

    let resolvedUrl: string;
    try {
      resolvedUrl = await invoke<string>("resolve_url", { input: rawInput });
    } catch {
      // Fallback if Tauri isn't available (dev preview)
      resolvedUrl = rawInput.includes(".")
        ? `https://${rawInput}`
        : `https://www.google.com/search?q=${encodeURIComponent(rawInput)}`;
    }

    updateTab(tab.id, {
      url: resolvedUrl,
      title: getDomain(resolvedUrl),
      isLoading: true,
    });
    setOmniboxValue(resolvedUrl);

    // Simulate loading completion (in a real Tauri WebView you'd hook into page-load events)
    setTimeout(() => {
      updateTab(activeTabId(), { isLoading: false });
    }, 1200);
  }

  function handleOmniboxKeyDown(e: KeyboardEvent) {
    if (e.key === "Enter") {
      navigate(omniboxValue());
      omniboxRef?.blur();
    } else if (e.key === "Escape") {
      setOmniboxValue(activeTab()?.url ?? "");
      omniboxRef?.blur();
    }
  }

  // ── Window Controls ──────────────────────────────────────────────────────────

  async function handleMinimize() {
    try {
      await invoke("minimize_window");
    } catch {}
  }

  async function handleMaximize() {
    try {
      await invoke("toggle_maximize");
      setIsMaximized((v) => !v);
    } catch {}
  }

  async function handleClose() {
    try {
      await invoke("close_window");
    } catch {}
  }

  async function handleDragStart() {
    try {
      await invoke("start_drag");
    } catch {}
  }

  // ── Render ───────────────────────────────────────────────────────────────────

  return (
    <div class="flex flex-col h-screen w-screen overflow-hidden bg-[#dee1e6] font-['Google_Sans',_'Segoe_UI',_sans-serif]">
      
      {/* ── Chrome Frame / Title Bar ────────────────────────────────────────── */}
      <div
        class="flex items-end h-[42px] bg-[#dee1e6] px-2 pt-2 select-none"
        onMouseDown={handleDragStart}
      >
        {/* Window title (left spacer) */}
        <div class="flex items-center mr-1 flex-shrink-0">
          {/* Nyro logo dot */}
          <div class="w-3 h-3 rounded-full bg-gradient-to-br from-[#4285f4] to-[#34a853] mr-2 flex-shrink-0" />
        </div>

        {/* Tab strip */}
        <div
          class="flex items-end flex-1 overflow-x-auto overflow-y-hidden tabs-scrollbar gap-0 min-w-0"
          onMouseDown={(e) => e.stopPropagation()}
        >
          <For each={tabs()}>
            {(tab) => (
              <TabItem
                tab={tab}
                isActive={tab.id === activeTabId()}
                onActivate={() => setActiveTabId(tab.id)}
                onClose={() => closeTab(tab.id)}
              />
            )}
          </For>

          {/* New Tab button */}
          <button
            class="flex-shrink-0 w-8 h-8 mb-0.5 ml-1 rounded-full flex items-center justify-center hover:bg-[#c5c8cc] transition-colors"
            onClick={addTab}
            title="New tab"
            onMouseDown={(e) => e.stopPropagation()}
          >
            <svg viewBox="0 0 16 16" class="w-4 h-4 fill-[#5f6368]">
              <path d="M8 3v10M3 8h10" stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        {/* Window Controls (right side) */}
        <div
          class="flex items-center gap-1 ml-2 mb-1 flex-shrink-0"
          onMouseDown={(e) => e.stopPropagation()}
        >
          <button
            class="w-[46px] h-8 flex items-center justify-center hover:bg-[#c5c8cc] rounded transition-colors"
            onClick={handleMinimize}
            title="Minimize"
          >
            <svg viewBox="0 0 10 1" class="w-[10px] h-[1px]">
              <path d="M0 0.5h10" stroke="#5f6368" stroke-width="1" />
            </svg>
          </button>
          <button
            class="w-[46px] h-8 flex items-center justify-center hover:bg-[#c5c8cc] rounded transition-colors"
            onClick={handleMaximize}
            title={isMaximized() ? "Restore" : "Maximize"}
          >
            <svg viewBox="0 0 10 10" class="w-[10px] h-[10px]">
              <rect
                x="0.5" y="0.5" width="9" height="9"
                fill="none" stroke="#5f6368" stroke-width="1"
                rx={isMaximized() ? "0" : "1"}
              />
            </svg>
          </button>
          <button
            class="w-[46px] h-8 flex items-center justify-center hover:bg-[#c0392b] hover:rounded transition-colors group"
            onClick={handleClose}
            title="Close"
          >
            <svg viewBox="0 0 10 10" class="w-[10px] h-[10px]">
              <path
                d="M1 1l8 8M9 1l-8 8"
                stroke="#5f6368" stroke-width="1.2" stroke-linecap="round"
                class="group-hover:stroke-white transition-colors"
              />
            </svg>
          </button>
        </div>
      </div>

      {/* ── Toolbar ─────────────────────────────────────────────────────────── */}
      <div class="flex items-center gap-1 h-[48px] bg-white px-3 border-b border-[#e0e0e0]">
        
        {/* Navigation buttons */}
        <button
          class="toolbar-btn"
          title="Back"
          onClick={() => window.history?.back()}
        >
          <svg viewBox="0 0 20 20" class="w-5 h-5 fill-[#5f6368]">
            <path d="M12.5 15L7.5 10l5-5" stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none" />
          </svg>
        </button>

        <button
          class="toolbar-btn"
          title="Forward"
          onClick={() => window.history?.forward()}
        >
          <svg viewBox="0 0 20 20" class="w-5 h-5">
            <path d="M7.5 5l5 5-5 5" stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none" />
          </svg>
        </button>

        <button
          class="toolbar-btn"
          title="Refresh"
          onClick={() => navigate(activeTab()?.url ?? "")}
        >
          <svg viewBox="0 0 20 20" class="w-5 h-5">
            <path
              d="M15.5 9A5.5 5.5 0 104.5 13.5"
              stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" fill="none"
            />
            <path d="M4 10.5L4.5 13.5 7.5 13" stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none" />
          </svg>
        </button>

        <button
          class="toolbar-btn"
          title="Home"
          onClick={() => navigate("https://www.google.com")}
        >
          <svg viewBox="0 0 20 20" class="w-5 h-5">
            <path d="M3 9.5L10 3l7 6.5M5 8.5V17h4v-4h2v4h4V8.5" stroke="#5f6368" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none" />
          </svg>
        </button>

        {/* ── Omnibox ── */}
        <div
          class={`omnibox flex-1 flex items-center h-[34px] mx-1 px-3 rounded-full border transition-all duration-150 ${
            omniboxFocused()
              ? "border-[#1a73e8] shadow-[0_0_0_3px_rgba(26,115,232,0.15)] bg-white"
              : "border-transparent bg-[#f1f3f4] hover:bg-[#e8eaed]"
          }`}
        >
          {/* Lock / search icon */}
          <Show
            when={omniboxFocused()}
            fallback={
              <svg viewBox="0 0 16 16" class="w-4 h-4 fill-[#5f6368] mr-2 flex-shrink-0">
                <circle cx="7" cy="7" r="4.5" stroke="#5f6368" stroke-width="1.3" fill="none" />
                <path d="M10.5 10.5l3 3" stroke="#5f6368" stroke-width="1.3" stroke-linecap="round" />
              </svg>
            }
          >
            <svg viewBox="0 0 16 16" class="w-4 h-4 mr-2 flex-shrink-0">
              <rect x="3" y="7" width="10" height="8" rx="1" stroke="#1a73e8" stroke-width="1.2" fill="none" />
              <path d="M5.5 7V5a2.5 2.5 0 015 0v2" stroke="#1a73e8" stroke-width="1.2" stroke-linecap="round" fill="none" />
            </svg>
          </Show>

          <input
            ref={omniboxRef}
            type="text"
            class="flex-1 bg-transparent outline-none text-[14px] text-[#202124] placeholder-[#9aa0a6] caret-[#1a73e8] min-w-0"
            value={omniboxValue()}
            placeholder="Search Google or type a URL"
            onFocus={() => {
              setOmniboxFocused(true);
              setTimeout(() => omniboxRef?.select(), 0);
            }}
            onBlur={() => {
              setOmniboxFocused(false);
              setOmniboxValue(activeTab()?.url ?? "");
            }}
            onInput={(e) => setOmniboxValue(e.currentTarget.value)}
            onKeyDown={handleOmniboxKeyDown}
          />

          {/* Bookmark star */}
          <Show when={!omniboxFocused()}>
            <button class="ml-2 opacity-60 hover:opacity-100 transition-opacity" title="Bookmark">
              <svg viewBox="0 0 16 16" class="w-4 h-4">
                <path d="M8 1l1.8 3.6L14 5.5l-3 2.9.7 4.1L8 10.5l-3.7 1.9.7-4.1L2 5.5l4.2-.9L8 1z"
                  stroke="#5f6368" stroke-width="1.1" fill="none" stroke-linejoin="round" />
              </svg>
            </button>
          </Show>
        </div>

        {/* Extension / menu area */}
        <div class="flex items-center gap-0.5">
          {/* Extensions placeholder */}
          <button class="toolbar-btn" title="Extensions">
            <svg viewBox="0 0 20 20" class="w-5 h-5">
              <path d="M12 2H8L7 5H4l1 4H3l2 4 3-1 1 3h2l1-3 3 1 2-4H15l1-4h-3L12 2z"
                stroke="#5f6368" stroke-width="1.3" fill="none" stroke-linejoin="round" />
            </svg>
          </button>

          {/* Profile */}
          <button
            class="w-7 h-7 rounded-full bg-[#1a73e8] flex items-center justify-center ml-0.5"
            title="Profile"
          >
            <span class="text-white text-[11px] font-semibold">N</span>
          </button>

          {/* Chrome menu (⋮) */}
          <button class="toolbar-btn ml-0.5" title="Customize and control Nyro">
            <svg viewBox="0 0 20 20" class="w-5 h-5">
              <circle cx="10" cy="4" r="1.2" fill="#5f6368" />
              <circle cx="10" cy="10" r="1.2" fill="#5f6368" />
              <circle cx="10" cy="16" r="1.2" fill="#5f6368" />
            </svg>
          </button>
        </div>
      </div>

      {/* ── Webview Placeholder ────────────────────────────────────────────── */}
      {/* In production Tauri, a <webview> tag or Tauri's native WebviewWindow   */}
      {/* renders here. This placeholder reflects the active tab state.          */}
      <div class="flex-1 bg-white flex flex-col items-center justify-center overflow-hidden relative">
        <Show
          when={activeTab()?.isLoading}
          fallback={
            <div class="text-center select-none pointer-events-none">
              {/* Nyro new-tab splash (shown when on google.com / new tab) */}
              <div class="mb-6 opacity-10">
                <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#4285f4] via-[#34a853] to-[#fbbc04] mx-auto" />
              </div>
              <p class="text-[#bdc1c6] text-sm">
                {activeTab()?.url === "https://www.google.com"
                  ? "New Tab · Nyro Browser"
                  : getDomain(activeTab()?.url ?? "")}
              </p>
              <p class="text-[#dadce0] text-xs mt-1">
                WebView renders here in the packaged app
              </p>
            </div>
          }
        >
          {/* Loading bar */}
          <div class="absolute top-0 left-0 right-0 h-[3px] bg-[#f1f3f4] overflow-hidden">
            <div class="loading-bar h-full bg-[#1a73e8]" />
          </div>
          <div class="text-[#5f6368] text-sm animate-pulse">
            Loading {getDomain(activeTab()?.url ?? "")}…
          </div>
        </Show>
      </div>
    </div>
  );
};

export default App;
