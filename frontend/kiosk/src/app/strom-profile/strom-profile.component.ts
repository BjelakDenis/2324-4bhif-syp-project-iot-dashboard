import { Component } from '@angular/core';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { ScaleType } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-strom-profile',
  standalone: true,
  imports: [NgxChartsModule],   // wichtig für Charts!
  templateUrl: './strom-profile.component.html',
  styleUrls: []
})

export class StromProfileComponent {
  view: [number, number] = [700, 400];

  multi = [
    {
      name: "Verbrauch",
      series: [
        { name: "20.06.", value: 19 },
        { name: "21.06.", value: 18 },
        { name: "22.06.", value: 18 },
        { name: "23.06.", value: 18 },
        { name: "24.06.", value: 17 },
        { name: "25.06.", value: 27 },
        { name: "26.06.", value: 26 }
      ]
    },
    {
      name: "Einspeisung",
      series: [
        { name: "20.06.", value: 25 },
        { name: "21.06.", value: 23 },
        { name: "22.06.", value: 28 },
        { name: "23.06.", value: 28 },
        { name: "24.06.", value: 28 },
        { name: "25.06.", value: 24 },
        { name: "26.06.", value: 27 }
      ]
    },
    {
      name: "Bezug",
      series: [
        { name: "20.06.", value: 10 },
        { name: "21.06.", value: 8 },
        { name: "22.06.", value: 7 },
        { name: "23.06.", value: 8 },
        { name: "24.06.", value: 8 },
        { name: "25.06.", value: 15 },
        { name: "26.06.", value: 14 }
      ]
    }
  ];

  colorScheme = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#4CAF50', '#03A9F4']
  };
  

  showXAxis = true;
  showYAxis = true;
  showLegend = true;
  legendTitle = 'Legende';
}
