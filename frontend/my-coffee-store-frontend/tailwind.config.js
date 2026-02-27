/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // 品牌色 - 咖啡主题
        primary: '#2A1A15',
        'primary-dark': '#1F130F',
        'primary-darker': '#1A110E',
        accent: '#D4A574',
        'accent-light': '#EADBC9',
        'accent-bg': '#EFE4D7',
        // 背景色
        background: '#F7F1E8',
        surface: '#F1E7DB',
        'surface-light': '#FFF9F0',
        // 文字色
        'text-primary': '#2A1A15',
        'text-secondary': '#5B4035',
        'text-light': '#6A4E43',
        'text-muted': '#9A7E6F',
        // 辅助色
        brown: '#C5BEB6',
        gold: '#DCCCB9',
        'gold-dark': '#D8C8B4',
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
        'pill': '12px',
      },
      spacing: {
        'section': '80px',
        'container': '60px',
      },
    },
  },
  plugins: [],
}
