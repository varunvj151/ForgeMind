/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: 'rgb(var(--color-bg-primary) / <alpha-value>)',
        surface: 'rgb(var(--color-bg-secondary) / <alpha-value>)',
        panel: 'rgb(var(--color-bg-tertiary) / <alpha-value>)',
        interactive: 'rgb(var(--color-bg-interactive) / <alpha-value>)',
        primary: 'rgb(var(--color-accent-primary) / <alpha-value>)',
        secondary: 'rgb(var(--color-accent-secondary) / <alpha-value>)',
        border: 'rgb(var(--color-border) / <alpha-value>)',
        'border-subtle': 'rgb(var(--color-border-subtle) / <alpha-value>)',
      },
      textColor: {
        primary: 'rgb(var(--color-text-primary) / <alpha-value>)',
        secondary: 'rgb(var(--color-text-secondary) / <alpha-value>)',
        muted: 'rgb(var(--color-text-muted) / <alpha-value>)',
      },
    },
  },
  plugins: [],
}
