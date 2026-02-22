import { apiClient } from "./client";

export interface MedicalEntry {
  id?: number;
  entryType?: string;
  content?: string;
  entryDate?: string;
}

export interface MedicalRecord {
  id: number;
  patientId: number;
  allergies?: string;
  currentMedications?: string;
  chronicConditions?: string;
  familyHistory?: string;
  entries?: MedicalEntry[];
}

export interface MedicalRecordCreate {
  allergies?: string;
  currentMedications?: string;
  chronicConditions?: string;
  familyHistory?: string;
}

export function fetchMedicalRecord(id: number) {
  return apiClient.get<MedicalRecord>(`/api/medical-records/${id}`);
}

export function fetchMedicalRecordByPatient(patientId: number) {
  return apiClient.get<MedicalRecord>(`/api/medical-records/patient/${patientId}`);
}

export function ensureMedicalRecord(patientId: number) {
  return apiClient.get<MedicalRecord>(`/api/medical-records/patient/${patientId}/ensure`);
}

export function createMedicalRecord(patientId: number) {
  return apiClient.post<MedicalRecord>(`/api/medical-records/patient/${patientId}`);
}

export function updateMedicalRecord(id: number, body: Partial<MedicalRecordCreate>) {
  return apiClient.put<MedicalRecord>(`/api/medical-records/${id}`, body);
}
