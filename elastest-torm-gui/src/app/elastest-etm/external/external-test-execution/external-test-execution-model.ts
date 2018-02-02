import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';

export class ExternalTestExecutionModel {
  id: number;
  monitoringIndex: string;
  fields: any;
  result: string;
  externalId: string;
  externalSystemId: string;
  exTestCase: ExternalTestCaseModel;

  constructor() {
    this.id = 0;
    this.monitoringIndex = '';
    this.fields = undefined;
    this.exTestCase = undefined;
  }
}
