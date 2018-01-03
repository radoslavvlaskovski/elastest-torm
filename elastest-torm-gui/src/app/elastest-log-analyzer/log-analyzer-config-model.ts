export class LogAnalyzerConfigModel {
    id: number;
    columnsState: any; // Object
    columnsConfig: string; // JSON string of Object

    constructor(jsonString: string = undefined) {
        this.id = 0;
        this.columnsState = undefined;
        this.columnsConfig = '';
    }

    public generatColumnsConfigJson(): void {
        this.columnsConfig = JSON.stringify(this.columnsState);
    }
}