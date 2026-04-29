import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        cyber: {
          bg: '#0e1512',
          base: '#09100d',
          surface: '#161d1b',
          panel: '#1a211f',
          panelHigh: '#242c29',
          border: '#3c4a45',
          outline: '#85948e',
          text: '#dde4e0',
          muted: '#bacac3',
          cyan: '#38debb',
          cyanBright: '#5ffbd6',
          purple: '#d7baff',
          purpleDeep: '#593090',
          emerald: '#31e368',
          danger: '#ffb4ab',
        },
      },
      fontFamily: {
        display: ['Space Grotesk', 'Inter', 'system-ui', 'sans-serif'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Consolas', 'monospace'],
      },
      borderRadius: {
        glass: '1rem',
      },
      boxShadow: {
        glow: '0 20px 40px rgba(56, 222, 187, 0.12)',
      },
      backdropBlur: {
        glass: '12px',
      },
    },
  },
  plugins: [],
} satisfies Config;
