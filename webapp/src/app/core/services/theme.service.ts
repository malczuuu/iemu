import { Injectable, signal, Signal } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'iemu.theme';
const PREFERS_DARK = '(prefers-color-scheme: dark)';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly state = signal<Theme>('light');
  private readonly media = window.matchMedia(PREFERS_DARK);

  public constructor() {
    const stored = this.readStored();
    if (stored !== null) {
      this.state.set(stored);
      this.apply(stored);
    } else {
      this.state.set(this.media.matches ? 'dark' : 'light');
      this.media.addEventListener('change', (e) => {
        if (this.readStored() === null) {
          this.state.set(e.matches ? 'dark' : 'light');
        }
      });
    }
  }

  public theme(): Signal<Theme> {
    return this.state.asReadonly();
  }

  public toggle(): void {
    const next: Theme = this.state() === 'dark' ? 'light' : 'dark';
    this.state.set(next);
    localStorage.setItem(STORAGE_KEY, next);
    this.apply(next);
  }

  private readStored(): Theme | null {
    const v = localStorage.getItem(STORAGE_KEY);
    return v === 'light' || v === 'dark' ? v : null;
  }

  private apply(theme: Theme): void {
    document.documentElement.setAttribute('data-theme', theme);
  }
}
