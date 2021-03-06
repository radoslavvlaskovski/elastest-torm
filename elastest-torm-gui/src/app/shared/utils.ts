export class CompleteUrlObj extends URL {
  queryParams: any;
}

export function getUrlQueryParams(url: string): any {
  let hash: string[];
  let myJson: any = {};
  let hashes: string[] = url.slice(url.indexOf('?') + 1).split('&');
  for (let i: number = 0; i < hashes.length; i++) {
    hash = hashes[i].split('=');
    myJson[hash[0]] = hash[1];
  }
  return myJson;
}

export function getUrlObj(url: string): CompleteUrlObj {
  let urlObj: any = new URL(url);
  urlObj.queryParams = getUrlQueryParams(url);
  return urlObj;
}

export type defaultResult =
  | 'SUCCESS'
  | 'PASSED'
  | 'FAIL'
  | 'FAILED'
  | 'STOPPED'
  | 'NOT_EXECUTED'
  | 'ERROR'
  | 'BLOCKED'
  | 'SKIPPED'
  | '';

export function getResultIconByString(result: defaultResult | string): any {
  let icon: any = {
    name: '',
    color: '',
    result: result,
  };

  switch (result) {
    case 'SUCCESS':
    case 'PASSED':
      icon.name = 'check_circle';
      icon.color = '#669a13';
      break;
    case 'FAIL':
    case 'FAILED':
      icon.name = 'error';
      icon.color = '#c82a0e';
      break;
    case 'STOPPED':
      icon.name = 'indeterminate_check_box';
      icon.color = '#c82a0e';
      break;
    case 'NOT_EXECUTED':
    case 'SKIPPED':
      icon.name = 'indeterminate_check_box';
      icon.color = '#666666';
      break;
    case 'ERROR':
      icon.name = 'do_not_disturb';
      icon.color = '#c82a0e';
      break;
    case 'BLOCKED':
      icon.name = 'warning';
      icon.color = '#ffac2f';
      break;
    default:
      icon.color = '#000000de';
      break;
  }

  return icon;
}

// Usage!
// sleep(500).then(() => {
// Do something after the sleep!
// })
export function sleep(milliseconds: number): Promise<any> {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

export async function asyncSleep(milliseconds: number): Promise<any> {
  return await sleep(milliseconds);
}

export function getDateIso8601Pattern(): string {
  return 'yyyy-MM-ddTHH:mm:sssZ';
}

export function isString(s): boolean {
  return typeof s === 'string' || s instanceof String;
}

export function removeDuplicates(arr: any[]): any[] {
  let uniqueArray: any[] = [];
  for (let i = 0; i < arr.length; i++) {
    if (uniqueArray.indexOf(arr[i]) === -1) {
      uniqueArray.push(arr[i]);
    }
  }
  return uniqueArray;
}
