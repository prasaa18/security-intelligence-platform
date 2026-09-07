import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, tap } from 'rxjs';

interface AuthSession {
  username: string;
  basicToken: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'security-intel-auth';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<boolean> {
    const basicToken = btoa(`${username}:${password}`);
    this.writeSession({ username, basicToken });

    return this.http.get('/api/dashboard/summary').pipe(
      map(() => true),
      tap({ error: () => this.clearSession() }),
      catchError(() => of(false))
    );
  }

  logout(): void {
    this.clearSession();
  }

  getUsername(): string | null {
    return this.readSession()?.username || null;
  }

  getAuthorizationHeader(): string | null {
    const session = this.readSession();
    return session ? `Basic ${session.basicToken}` : null;
  }

  isAuthenticated(): boolean {
    return !!this.readSession();
  }

  private readSession(): AuthSession | null {
    if (typeof sessionStorage === 'undefined') return null;
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      this.clearSession();
      return null;
    }
  }

  private writeSession(session: AuthSession): void {
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem(this.storageKey, JSON.stringify(session));
    }
  }

  private clearSession(): void {
    if (typeof sessionStorage !== 'undefined') sessionStorage.removeItem(this.storageKey);
  }
}
