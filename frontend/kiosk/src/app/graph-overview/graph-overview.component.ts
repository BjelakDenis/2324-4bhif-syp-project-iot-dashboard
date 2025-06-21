import { Component, OnInit } from '@angular/core';
import { Graph } from "../model/Graph";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { HttpClient } from "@angular/common/http";
import { NgForOf, NgIf } from "@angular/common";
import { GraphComponent } from "../graph/graph.component";
import { FormsModule } from '@angular/forms';
import { Subscription, timer } from 'rxjs';
import { Duration } from '../model/Duration';
import { WeatherComponent } from "../weather/weather.component";
import { RouterLink } from "@angular/router";
import { SensorboxOverviewComponent } from "../sensorbox-overview/sensorbox-overview.component";
import { NgxChartsModule, ScaleType } from '@swimlane/ngx-charts';
import { StromProfileComponent } from "../strom-profile/strom-profile.component";

@Component({
  selector: 'app-graph-overview',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    GraphComponent,
    FormsModule,
    WeatherComponent,
    RouterLink,
    SensorboxOverviewComponent,
    NgxChartsModule,
    StromProfileComponent,
  ],
  templateUrl: './graph-overview.component.html',
  styleUrls: ['./graph-overview.component.css']
})
export class GraphOverviewComponent implements OnInit {
  public graphs: Graph[] = [];
  public currentIndex = -1;
  public currentGraph: Graph | null = null;
  public iFrameLink: SafeResourceUrl | string = '';// Unterstützt sowohl SafeResourceUrl als auch string

  public isMonthSelected: boolean = false;
  public kioskMode: boolean = true;
  public interval: number = 15;
  subscription!: Subscription;

  public currentPowerWatt: number = 0;
  public showPvData: boolean = true;
  public years: number[] = [2024, 2025];
  public selectedYear: number = new Date().getFullYear();

  public durations: Duration[] = [
    new Duration("5m", "5 minutes"),
    new Duration("1h", "1 hour"),
    new Duration("4h", "4 hours"),
    new Duration("1d", "1 day"),
    new Duration("2d", "2 days"),
    new Duration("7d", "1 week"),
    new Duration("30d", "1 month"),
    new Duration("365d", "1 year")
  ];
  public selectedDuration: Duration = this.durations[3]; // "1 day"

  public selectedMonthYear: { year: number, month: number } = { year: new Date().getFullYear(), month: new Date().getMonth() };
  public visible: boolean = false;

  public months: string[] = [
    "Januar", "Februar", "März", "April", "Mai", "Juni",
    "Juli", "August", "September", "Oktober", "November", "Dezember"
  ];
  public selectedMonth: number = new Date().getMonth();

  public selectedWeek: number = 1; // Beispielwert, passe die Logik entsprechend an

  colorScheme = {
    name: 'cool',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#f44336', '#ffeb3b', '#e91e63', '#ff5722', '#ff9800', '#9c27b0', '#4caf50']
  };

  public selectedDate: string = ''; // Aktuelles Datum als String

  public kioskModeChecker(): void {
    console.log("Kiosk Mode Checker executed");
    // Füge hier die Logik für den Kiosk-Modus hinzu, falls erforderlich
  }

  private sanitizeAndInjectCss(link: string, from: number | string, to: number | string, css: string): SafeResourceUrl {
    const sanitizedLink = `${link}?from=${from}&to=${to}&css=${css}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(sanitizedLink);
  }

  private updateCurrentGraph(): void {
    if (this.graphs.length > 0) {
      this.currentGraph = this.graphs[0]; // Beispiel: Setze das erste Diagramm als aktuelles Diagramm
    } else {
      this.currentGraph = null;
    }
  }

  public deactivateKioskMode(): void {
    console.log("Kiosk Mode deaktiviert");
    this.kioskMode = false;
  }

  public compareDurations(d1: Duration, d2: Duration): boolean {
    return d1.short === d2.short;
  }

  public selectMonthYearCombo(): void {
    console.log("Monat und Jahr ausgewählt:", this.selectedMonthYear);
    // Füge hier die Logik hinzu, falls erforderlich
  }

  public toggleDataMode(): void {
    console.log("Datenmodus umgeschaltet:", this.showPvData ? "PV-Daten" : "Sensorboxen");
  }

  public logButtonClick(): void {
    console.log('Button wurde geklickt');
  }

  constructor(public sanitizer: DomSanitizer, public http: HttpClient) {}

  ngOnInit(): void {
    console.log("GraphOverviewComponent gestartet");
    const today = new Date();
    this.selectedDate = today.toISOString().substring(0, 10); // z.B. "2025-06-21"
    this.selectedMonthYear = { year: today.getFullYear(), month: today.getMonth() };
    this.selectedMonth = today.getMonth();
    this.selectedYear = today.getFullYear();
    this.isMonthSelected = false;

    // Setze die obere Leiste auf den aktuellen Tag
    this.updateTopBarDate(today);

    this.selectedDuration = this.durations[3]; // Default: "1 day"

    this.http.get<Graph[]>('assets/data/graph-data.json').subscribe((data) => {
      this.graphs = data;
      console.log(this.graphs.length + " graphs loaded");

      this.changeDuration(); // Daten laden, wenn verfügbar
    });

    this.kioskModeChecker();

    this.http.get<any>('http://localhost:8080/sensorbox/latest-values/Serverraum').subscribe(data => {
      if (data?.power) {
        this.currentPowerWatt = data.power;
      } else {
        console.warn('Keine power-Daten vorhanden:', data);
      }
    });
  }

  private updateTopBarDate(date: Date): void {
    const formattedDate = date.toLocaleDateString('de-DE', { year: 'numeric', month: 'long', day: 'numeric' });
    const topBarDateElement = document.querySelector('.top-bar-date');
    if (topBarDateElement) {
      topBarDateElement.textContent = formattedDate; // Beispiel: "21. Juni 2025"
    }
  }

  public changeDuration(): void {
    console.log("Changing Timeframe to: ", this.selectedDuration.short);

    // Monat ausblenden, wenn "1 week" ausgewählt ist
    if (this.selectedDuration.short === "1 week") {
      this.isMonthSelected = false;
      this.selectedMonthYear = { year: 0, month: 0 }; // Setze auf ein leeres Objekt statt null
    }

    this.updateGraphLinks();
  }

  private updateGraphLinks(): void {
    const customCss = encodeURIComponent(`
      .panel-container, .graph-panel {
        background-color: pink !important;
      }
      .flot-background {
        fill: white !important;
      }
      .bar rect {
        width: 10px !important;
        fill: #ff7e5f !important;
        rx: 4; ry: 4;
      }
      .axisLabel, .tickLabel {
        fill: #444 !important;
        font-size: 12px !important;
      }
    `);

    if (this.isMonthSelected) {
      const { from, to } = this.calculateStartAndEndOfMonth(this.selectedMonth, this.selectedYear);
      this.graphs.forEach(graph => {
        const safeUrl = this.sanitizeAndInjectCss(graph.iFrameLink, from, to, customCss);
        graph.iFrameLink = this.sanitizer.sanitize(1, safeUrl) || ''; // Konvertiere SafeResourceUrl in string
      });
    } else {
      const selectedDuration: string = this.selectedDuration.short;
      this.graphs.forEach(graph => {
        const safeUrl = this.sanitizeAndInjectCss(graph.iFrameLink, `now-${selectedDuration}`, `now`, customCss);
        graph.iFrameLink = this.sanitizer.sanitize(1, safeUrl) || ''; // Konvertiere SafeResourceUrl in string
      });
    }
  

    this.updateCurrentGraph();
  }

  calculateStartAndEndOfMonth(month: number, year: number): { from: number, to: number } {
    const startDate = new Date(year, month, 1, 0, 0, 0, 0);
    const endDate = new Date(year, month + 1, 0, 23, 59, 59, 999);

    // Stelle sicher, dass alle Tage des Monats durchlaufen werden
    const daysInMonth = [];
    for (let day = 1; day <= endDate.getDate(); day++) {
      daysInMonth.push(new Date(year, month, day).getTime());
    }

    return { from: startDate.getTime(), to: endDate.getTime() };
  }
}