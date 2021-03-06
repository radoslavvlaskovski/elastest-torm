export class ParameterModel {
  name: string;
  value: string;
  multiConfig: boolean;

  constructor(parameterJson: any = undefined) {
    if (!parameterJson) {
      this.name = '';
      this.value = '';
      this.multiConfig = false;
    } else {
      this.name = parameterJson.name;
      this.value = parameterJson.value;
      this.multiConfig =
        parameterJson.multiConfig !== undefined && parameterJson.multiConfig !== null ? parameterJson.multiConfig : false;
    }
  }
}
