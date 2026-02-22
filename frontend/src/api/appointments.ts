import { apiClient } from "./client";

export interface Appointment {
  id: number;
  patientId: number;
  doctorId: number;
  appointmentDateTime: string;
  durationMinutes?: number;
  status?: string;
  appointmentType?: string;
  reason?: string;
  notes?: string;
  roomNumber?: string;
}

export interface AppointmentCreate {
  patientId: number;
  doctorId: number;
  appointmentDateTime: string;
  durationMinutes?: number;
  appointmentType?: string;
  reason?: string;
  roomNumber?: string;
}

export function fetchAppointment(id: number) {
  return apiClient.get<Appointment>(`/api/appointments/${id}`);
}

export function fetchAppointmentsByPatient(patientId: number) {
  return apiClient.get<Appointment[]>(`/api/appointments/patient/${patientId}`);
}

export function fetchAppointmentsByDoctor(doctorId: number) {
  return apiClient.get<Appointment[]>(`/api/appointments/doctor/${doctorId}`);
}

export function createAppointment(body: AppointmentCreate) {
  return apiClient.post<Appointment>("/api/appointments", body);
}

export function updateAppointment(id: number, body: Partial<AppointmentCreate> & { status?: string }) {
  return apiClient.put<Appointment>(`/api/appointments/${id}`, body);
}

export function deleteAppointment(id: number) {
  return apiClient.delete(`/api/appointments/${id}`);
}
