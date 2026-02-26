/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#1A1A1A',
        accent: '#D4A574',
        'accent-light': '#E8C49E',
        background: '#FFFFFF',
        surface: '#F5F5F5',
        'text-primary': '#1A1A1A',
        'text-secondary': '#666666',
        'text-light': '#999999',
      },
      fontFamily: {
        georgia: ['Georgia', 'serif'],
        inter: ['Inter', 'sans-serif'],
      },
      fontSize: {
        'hero': ['42px', { lineHeight: '1.2', fontWeight: 'bold' }],
        'title': ['32px', { lineHeight: '1.3', fontWeight: 'bold' }],
        'subtitle': ['24px', { lineHeight: '1.4', fontWeight: '500' }],
        'body': ['16px', { lineHeight: '1.6', fontWeight: 'normal' }],
        'caption': ['14px', { lineHeight: '1.5', fontWeight: 'normal' }],
        'small': ['12px', { lineHeight: '1.4', fontWeight: 'normal' }],
      },
      borderRadius: {
        'card': '16px',
        'button': '8px',
        'input': '8px',
      },
      spacing: {
        'section': '80px',
        'container': '60px',
      },
    },
  },
  plugins: [],
}
