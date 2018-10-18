import { MetricsDataModel } from './metrics-data-model';
export class SingleMetricModel implements MetricsDataModel {
  name: Date | number;
  value: any;
  timestamp: any;

  constructor() {
    this.name = undefined;
    this.value = '';
    this.timestamp = '';
  }
}
