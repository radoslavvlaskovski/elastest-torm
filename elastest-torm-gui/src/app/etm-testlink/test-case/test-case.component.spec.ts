import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TLTestCaseComponent } from './test-case.component';

describe('TLTestCaseComponent', () => {
  let component: TLTestCaseComponent;
  let fixture: ComponentFixture<TLTestCaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TLTestCaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TLTestCaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
