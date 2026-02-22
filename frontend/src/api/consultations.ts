import { apiClient } from "./client";

export interface Consultation {
  consultationId: string;
  patientId: number;
  userId: number;
  consultationDate: string;
  consultationType?: string;
  diagnostic?: string;
  notes?: string;
  motif?: string;
  prescriptions?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ConsultationCreate {
  patientId: number;
  userId: number;
  consultationDate: string;
  consultationType?: string;
  notes?: string;
  motif?: string;
}

export function fetchConsultations() {
  return apiClient.get<Consultation[]>("/api/consultations");
}

export function fetchConsultation(id: string) {
  return apiClient.get<Consultation>(`/api/consultations/${id}`);
}

export function createConsultation(body: ConsultationCreate) {
  return apiClient.post<Consultation>("/api/consultations", body);
}

export function updateConsultation(id: string, body: Partial<ConsultationCreate>) {
  return apiClient.put<Consultation>(`/api/consultations/${id}`, body);
}

export function deleteConsultation(id: string) {
  return apiClient.delete(`/api/consultations/${id}`);
}
