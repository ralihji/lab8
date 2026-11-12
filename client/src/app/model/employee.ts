export class Employee {
  constructor(
    public id: string,
    public name: string,
    public dateOfBirth: string,
    public city: string,
    public salary: number,
    public gender?: string,
    public email?: string
  ) {}
}
