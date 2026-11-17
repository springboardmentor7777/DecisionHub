/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Hanken Grotesk"', 'system-ui', '-apple-system', '"Segoe UI"', 'Roboto', 'sans-serif'],
        display: ['"Bricolage Grotesque"', '"Hanken Grotesk"', 'system-ui', 'sans-serif'],
      },
      colors: {
        // Deep teal: primary brand colour
        brand: {
          50: '#effaf8',
          100: '#d6f0ec',
          200: '#afe0d9',
          300: '#7ccabf',
          400: '#49ada3',
          500: '#2c9087',
          600: '#1f726b',
          700: '#1b5c57',
          800: '#184a47',
          900: '#123a37',
          950: '#0a2422',
        },
        // Marigold: used sparingly for the "leading option" moment
        marigold: {
          50: '#fff8e6',
          100: '#ffedbd',
          300: '#fbd170',
          400: '#f6bb3b',
          500: '#e9a014',
          600: '#c77f0c',
        },
        paper: '#f5f8f6',
        ink: '#142321',
      },
      keyframes: {
        'grow-x': {
          from: { transform: 'scaleX(0)' },
          to: { transform: 'scaleX(1)' },
        },
      },
      animation: {
        'grow-x': 'grow-x 900ms cubic-bezier(0.22, 1, 0.36, 1) 350ms both',
      },
    },
  },
  plugins: [],
}
