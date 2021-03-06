import { TransformService } from '../elastest-etm/help/transform.service';

import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigModel } from './config-model';
import { Observable } from 'rxjs/Rx';
import { CoreServiceModel } from '../elastest-etm/models/core-service.model';

@Injectable()
export class ConfigurationService {
  public configModel: ConfigModel;
  protocol: string;
  host: string;
  hostApi: string;

  devHost: string = 'localhost:4200';

  constructor(private http: Http, private transformService: TransformService) {
    console.log('Starting configuration.');
    this.protocol = window.location.protocol;
    this.host = window.location.host;
    console.log('protocol: ' + this.protocol);
    this.hostApi = (this.protocol === 'https:' ? 'https://' : 'http://') + this.host + '/api';
    console.log('hostapi: ' + this.hostApi);
  }

  load() {
    return new Promise((resolve, reject) => {
      this.getServicesInfo().subscribe((servicesInfo) => {
        let eusUrl: URL =
          servicesInfo.elasTestExecMode !== 'experimental' && servicesInfo.eusSSInstance !== null
            ? new URL(servicesInfo.eusSSInstance.urls.api)
            : null;

        let proxyHost: string = this.host;
        if (this.host.startsWith(this.devHost)) {
          proxyHost = this.host.replace(this.devHost, 'localhost:37000');
        }
        this.configModel = {
          hostName: window.location.hostname,
          host: (this.protocol === 'https:' ? 'https://' : 'http://') + this.host,
          proxyHost: (this.protocol === 'https:' ? 'https://' : 'http://') + proxyHost,
          hostApi: this.hostApi,
          hostEIM: (this.protocol === 'https:' ? 'https://' : 'http://') + environment.hostEIM + '/',
          hostWsServer: (this.protocol === 'https:' ? 'wss://' : 'ws://') + this.host + servicesInfo.rabbitPath,
          eusHost: eusUrl !== null ? eusUrl.hostname : null,
          eusPort: eusUrl !== null ? eusUrl.port : null,
          eusServiceUrlNoPath: (this.protocol === 'https:' ? 'https://' : 'http://') + environment.eus,
          eusServiceUrl:
            servicesInfo.elasTestExecMode !== 'experimental' && servicesInfo.eusSSInstance !== null
              ? this.protocol === 'https:'
                ? String(servicesInfo.eusSSInstance.urls.api).replace('http://', 'https://')
                : servicesInfo.eusSSInstance.urls.api
              : null,
          eusWebSocketUrl:
            servicesInfo.elasTestExecMode !== 'experimental' && servicesInfo.eusSSInstance !== null
              ? this.protocol === 'https:'
                ? String(servicesInfo.eusSSInstance.urls.eusWSapi).replace('ws://', 'wss://')
                : servicesInfo.eusSSInstance.urls.eusWSapi
              : null,
          elasTestExecMode: servicesInfo.elasTestExecMode,
          testLinkStarted: servicesInfo.testLinkStarted,
          httpsSecure: this.protocol === 'https' ? true : false,
          empGrafanaUrl: servicesInfo.empGrafanaUrl,
          edmCommandUrl: servicesInfo.edmCommandUrl,
        };

        resolve();
        console.log('The configuration is completed.');
      });
    });
  }

  public getServicesInfo(): Observable<any> {
    let url: string = this.hostApi + '/context/services/info';
    return this.http.get(url).map((response) => response.json());
  }

  public getWSHost(): Observable<any> {
    let url: string = this.hostApi + '/context/ws-host';
    return this.http.get(url).map((response) => response['_body']);
  }

  public getLogstashIp(): Observable<any> {
    let hostApi: string = this.configModel.hostApi;
    let url: string = hostApi + '/context/logstash/ip';
    return this.http.get(url).map((response) => response['_body']);
  }

  public getLogstashInfo(): Observable<any> {
    let hostApi: string = this.configModel.hostApi;
    let url: string = hostApi + '/context/logstash/info';
    return this.http.get(url).map((response) => response.json());
  }

  public getHelpInfo(): Observable<any> {
    let url: string = this.configModel.hostApi + '/context/help/info';
    return this.http.get(url).map((response) => response.json());
  }

  /* ********************* */
  /* *** Core services *** */
  /* ********************* */

  public getCoreServicesInfo(): Observable<CoreServiceModel[]> {
    let url: string = this.configModel.hostApi + '/context/coreservices/info';
    return this.http.get(url).map((response) => this.transformService.jsonToCoreServicesList(response.json()));
  }

  public getAllCoreServiceLogs(coreServiceName: string, withFollow: boolean): Observable<string> {
    let url: string = this.configModel.hostApi + '/context/coreservices/' + coreServiceName + '/logs';
    if (withFollow) {
      url += '/follow';
    }
    return this.http.get(url).map((response) => response['_body']);
  }

  public getSomeCoreServiceLogs(coreServiceName: string, amount: number, withFollow: boolean): Observable<string> {
    let url: string = this.configModel.hostApi + '/context/coreservices/' + coreServiceName + '/logs/' + amount;
    if (withFollow) {
      url += '/follow';
    }
    return this.http.get(url).map((response) => response['_body']);
  }

  public getCoreServiceLogsSince(coreServiceName: string, since: number, withFollow: boolean): Observable<string> {
    let url: string = this.configModel.hostApi + '/context/coreservices/' + coreServiceName + '/logs/since/' + since;
    if (withFollow) {
      url += '/follow';
    }
    return this.http.get(url).map((response) => response['_body']);
  }

  public logsWithTimestampToLogViewTraces(logs: string): any[] {
    return this.transformService.logsWithTimestampToLogViewTraces(logs);
  }
}
