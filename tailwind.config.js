// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Chrome / Nyro palette
        chrome: {
          frame:      "#dee1e6",   // window frame & inactive tabs
          toolbar:    "#ffffff",   // active tab + toolbar
          hover:      "#e8eaed",   // tab hover
          border:     "#c5c8cc",   // subtle border
          text:       "#202124",   // primary text
          secondary:  "#3c4043",   // secondary text
          muted:      "#5f6368",   // muted text / icons
          disabled:   "#9aa0a6",   // disabled state
          omnibox:    "#f1f3f4",   // omnibox background
        },
        google: {
          blue:       "#1a73e8",
          "blue-dark":"#1558b0",
          red:        "#ea4335",
          yellow:     "#fbbc04",
          green:      "#34a853",
        },
      },
      fontFamily: {
        "google-sans": ["'Google Sans'", "'Segoe UI'", "system-ui", "sans-serif"],
      },
      fontSize: {
        "2xs": ["11px", { lineHeight: "14px" }],
        "tab":  ["13px", { lineHeight: "16px" }],
      },
      height: {
        "tab":     "34px",
        "toolbar": "48px",
        "frame":   "42px",
      },
      minWidth: {
        "tab": "180px",
      },
      maxWidth: {
        "tab": "240px",
      },
      boxShadow: {
        "tab-active": "0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.08)",
        "omnibox-focus": "0 0 0 3px rgba(26,115,232,0.18)",
      },
      transitionDuration: {
        "fast": "100ms",
        "med":  "200ms",
      },
      keyframes: {
        "load-progress": {
          "0%":   { width: "0%",   opacity: "1" },
          "30%":  { width: "40%"                },
          "60%":  { width: "70%"                },
          "85%":  { width: "88%"                },
          "95%":  { width: "95%"                },
          "100%": { width: "100%", opacity: "0" },
        },
        spin: {
          to: { transform: "rotate(360deg)" },
        },
      },
      animation: {
        "load-progress": "load-progress 1.1s cubic-bezier(0.4,0,0.2,1) forwards",
        "spin-fast":     "spin 0.8s linear infinite",
      },
    },
  },
  plugins: [],
};
