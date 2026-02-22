import { apiClient } from "./client";

export interface Patient {
  id: number;
  nationalId?: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: string;
  email?: string;
  phoneNumber?: string;
  address?: string;
  bloodType?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
}

export interface PatientCreate {
  firstName: string;
  lastName: string;
  nationalId?: string;
  dateOfBirth?: string;
  gender?: string;
  email?: string;
  phoneNumber?: string;
  address?: string;
  bloodType?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
}

export function fetchPatients() {
  return apiClient.get<Patient[]>("/api/patients");
}

export function fetchPatient(id: number) {
  return apiClient.get<Patient>(`/api/patients/${id}`);
}

export function createPatient(body: PatientCreate) {
  return apiClient.post<Patient>("/api/patients", body);
}

export function updatePatient(id: number, body: Partial<PatientCreate>) {
  return apiClient.put<Patient>(`/api/patients/${id}`, body);
}

export function deletePatient(id: number) {
  return apiClient.delete(`/api/patients/${id}`);
}

export function searchPatients(query: string) {
  return apiClient.get<Patient[]>("/api/patients/search", {
    params: { query },
  });
}
