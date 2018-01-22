import { ExecStatusValue, TestCaseModel, ExecTypeValue } from './test-case-model';
import { BuildModel } from './build-model';

export class TestCaseExecutionModel {
    id: number;
    buildId: number;
    testerId: number;
    executionTimeStamp: number;
    status: ExecStatusValue;
    testPlanId: number;
    testCaseVersionId: number;
    testCaseVersionNumber: number;
    executionType: ExecTypeValue;
    notes: string;

    constructor() {
        this.id = 0;
    }
}