// Nyro Browser - Tauri Backend
// src-tauri/src/main.rs

#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use tauri::{command, Runtime, Window};

/// Allows the frameless window to be dragged by the custom title bar.
/// Called from the frontend via Tauri's invoke() when mousedown fires on the drag region.
#[command]
async fn start_drag<R: Runtime>(window: Window<R>) -> Result<(), String> {
    window.start_dragging().map_err(|e| e.to_string())
}

/// Sanitizes and resolves a raw omnibox input into a navigable URL string.
/// - If the input is a valid URL (contains "." and no spaces, or starts with a scheme),
///   it is returned as-is (with https:// prepended if no scheme is present).
/// - Otherwise, the input is treated as a search query and wrapped in a Google Search URL.
#[command]
fn resolve_url(input: String) -> String {
    let trimmed = input.trim().to_string();

    // Check if it already has a scheme (http://, https://, file://, etc.)
    if trimmed.starts_with("http://")
        || trimmed.starts_with("https://")
        || trimmed.starts_with("file://")
    {
        return trimmed;
    }

    // Heuristic: treat as URL if it looks like a domain (has a dot, no spaces)
    let looks_like_url = trimmed.contains('.')
        && !trimmed.contains(' ')
        && !trimmed.starts_with('.');

    if looks_like_url {
        format!("https://{}", trimmed)
    } else {
        // Treat as a search query
        let encoded = urlencoding::encode(&trimmed);
        format!("https://www.google.com/search?q={}", encoded)
    }
}

/// Returns the current Nyro Browser version string.
#[command]
fn get_version() -> String {
    env!("CARGO_PKG_VERSION").to_string()
}

/// Minimizes the window.
#[command]
async fn minimize_window<R: Runtime>(window: Window<R>) -> Result<(), String> {
    window.minimize().map_err(|e| e.to_string())
}

/// Toggles maximize/restore on the window.
#[command]
async fn toggle_maximize<R: Runtime>(window: Window<R>) -> Result<(), String> {
    if window.is_maximized().map_err(|e| e.to_string())? {
        window.unmaximize().map_err(|e| e.to_string())
    } else {
        window.maximize().map_err(|e| e.to_string())
    }
}

/// Closes the window (and the app if it's the last window).
#[command]
async fn close_window<R: Runtime>(window: Window<R>) -> Result<(), String> {
    window.close().map_err(|e| e.to_string())
}

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![
            start_drag,
            resolve_url,
            get_version,
            minimize_window,
            toggle_maximize,
            close_window,
        ])
        .setup(|app| {
            // In debug mode, open the devtools automatically.
            #[cfg(debug_assertions)]
            {
                let window = app.get_window("main").unwrap();
                window.open_devtools();
            }
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running Nyro Browser");
}
