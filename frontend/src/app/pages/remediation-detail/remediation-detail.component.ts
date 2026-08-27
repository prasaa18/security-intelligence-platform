import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-remediation-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './remediation-detail.component.html',
  styleUrls: ['./remediation-detail.component.css']
})
export class RemediationDetailComponent implements OnInit {
  remediationItem: any = null;
  loading = true;
  error: string | null = null;

  constructor(
    public route: ActivatedRoute,
    public router: Router,
    private location: Location,
    private apiService: ApiService
  ) {}

  goBack(): void { this.location.back(); }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadRemediationItem(id);
    } else {
      this.error = 'Invalid remediation item ID';
      this.loading = false;
    }
  }

  loadRemediationItem(id: string): void {
    this.loading = true;
    this.apiService.getRemediationItemById(id).subscribe({
      next: (item) => {
        this.remediationItem = item;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load remediation item';
        this.loading = false;
        console.error('Error loading remediation item:', err);
      }
    });
  }

  updateStatus(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const newStatus = target.value;
    if (this.remediationItem?.id) {
      this.apiService.updateRemediationStatus(this.remediationItem.id, newStatus).subscribe({
        next: () => {
          this.loadRemediationItem(this.remediationItem.id);
        },
        error: (err) => {
          console.error('Error updating status:', err);
          alert('Failed to update status');
        }
      });
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  }
}