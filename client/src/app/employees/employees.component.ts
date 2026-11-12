import {Component} from '@angular/core';
import {EmployeeService} from "../service/employee.service";
import {Employee} from "../model/employee";
import { RouterLink } from '@angular/router';
import { NgFor, AsyncPipe, DatePipe } from '@angular/common';

@Component({
    selector: 'app-employees',
    templateUrl: './employees.component.html',
    styleUrls: ['./employees.component.css'],
    standalone: true,
    imports: [RouterLink, NgFor, AsyncPipe, DatePipe]
})
export class EmployeesComponent {
  constructor(protected employees: EmployeeService) {
  }

  identify(index: number, employee: Employee): string {
    return employee.id;
  }
}
