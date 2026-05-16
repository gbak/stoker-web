/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        stoker: {
          orange: '#ff6b35',
          dark: '#1a1a2e',
          panel: '#16213e',
          accent: '#0f3460',
        }
      }
    },
  },
  plugins: [],
}
