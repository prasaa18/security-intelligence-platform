import { Component, ViewChild } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CommandPaletteComponent } from './components/command-palette/command-palette.component';
import { ToastContainerComponent } from './components/toast-container/toast-container.component';
import { ThemeService } from './services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterModule,
    CommonModule,
    CommandPaletteComponent,
    ToastContainerComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Security Intelligence Platform';

  @ViewChild('commandPalette') commandPalette!: CommandPaletteComponent;

  constructor(public themeService: ThemeService) {}

  openCommandPalette() {
    if (this.commandPalette) {
      this.commandPalette.open();
    }
  }

  toggleTheme() {
    this.themeService.toggleTheme();
  }
}