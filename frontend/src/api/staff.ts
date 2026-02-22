import { apiClient } from "./client";

export interface Staff {
  id: number;
  employeeId?: string;
  firstName: string;
  lastName: string;
  email?: string;
  phoneNumber?: string;
  role?: string;
  specialty?: string;
  department?: string;
  licenseNumber?: string;
  hireDate?: string;
  active?: boolean;
}

export function fetchStaff() {
  return apiClient.get<Staff[]>("/api/staff");
}

export function fetchStaffMember(id: number) {
  return apiClient.get<Staff>(`/api/staff/${id}`);
}
