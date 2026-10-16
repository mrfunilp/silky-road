import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { MyItemsComponent } from './my-items.component';

describe('MyItemsComponent', () => {
  let component: MyItemsComponent;
  let fixture: ComponentFixture<MyItemsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyItemsComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MyItemsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
