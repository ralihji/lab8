import { Employee } from './employee';

describe('Employee', () => {
  it('should create an instance', () => {
    expect(new Employee("andrew", new Date(), "London", 1000)).toBeTruthy();
  });
});
