import { TestCaseStepModel } from '../models/test-case-step-model';
import { TLTestCaseModel } from '../models/test-case-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'testlink-test-case',
  templateUrl: './test-case.component.html',
  styleUrls: ['./test-case.component.scss']
})
export class TLTestCaseComponent implements OnInit {


  testCase: TLTestCaseModel;
  showSpinner: boolean = true;
  testCaseSteps: TestCaseStepModel[] = [];

  // TestCaseStep Data
  testCaseStepColumns: any[] = [
    { name: 'id', label: 'Id' },
    // { name: 'testCaseVersionId', label: 'Case Version ID' },
    { name: 'number', label: 'Number' },
    { name: 'actions', label: 'Actions' },
    { name: 'expectedResults', label: 'Expected Results' },
    { name: 'active', label: 'Active' },
    { name: 'executionType', label: 'Exec Type' },

    // { name: 'options', label: 'Options' },
  ];

  constructor(private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Case');
    this.testCase = new TLTestCaseModel();
    this.loadCase();
  }

  loadCase(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.testLinkService.getTestCaseById(params['caseId']))
        .subscribe((testCase: TLTestCaseModel) => {
          this.testCase = testCase;
          this.showSpinner = false;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);
        });
    }
  }
}
