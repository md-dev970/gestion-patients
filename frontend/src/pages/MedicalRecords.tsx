import { useEffect, useState } from "react";
import {
  Box,
  Button,
  Field,
  Heading,
  Text,
  Spinner,
  Alert,
  HStack,
  Stack,
} from "@chakra-ui/react";
import {
  fetchMedicalRecordByPatient,
  ensureMedicalRecord,
  type MedicalRecord,
} from "../api/medical-records";
import { fetchPatients } from "../api/patients";
import type { Patient } from "../api/patients";

export function MedicalRecords() {
  const [patientId, setPatientId] = useState("");
  const [patients, setPatients] = useState<Patient[]>([]);
  const [record, setRecord] = useState<MedicalRecord | null>(null);
  const [loading, setLoading] = useState(false);
  const [, setLoadingPatients] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchPatients()
      .then((res) => setPatients(res.data ?? []))
      .catch(() => setPatients([]))
      .finally(() => setLoadingPatients(false));
  }, []);

  function load() {
    const id = Number(patientId);
    if (!patientId || Number.isNaN(id)) {
      setError("Select a patient");
      return;
    }
    setError("");
    setLoading(true);
    setRecord(null);
    fetchMedicalRecordByPatient(id)
      .then((res) => setRecord(res.data))
      .catch((err) => {
        if (err.response?.status === 404) setRecord(null);
        else setError(err.response?.data?.message ?? "Failed to load record");
      })
      .finally(() => setLoading(false));
  }

  function ensureOrCreate() {
    const id = Number(patientId);
    if (!patientId || Number.isNaN(id)) return;
    setError("");
    setLoading(true);
    setRecord(null);
    ensureMedicalRecord(id)
      .then((res) => setRecord(res.data))
      .catch((err) => {
        setError(err.response?.data?.message ?? "Failed to get or create record");
      })
      .finally(() => setLoading(false));
  }

  return (
    <Box>
      <Heading size="lg" mb={4}>
        Medical records
      </Heading>
      <HStack gap={4} mb={4} flexWrap="wrap">
        <Field.Root style={{ minWidth: 200 }}>
          <Field.Label>Patient</Field.Label>
          <select
            value={patientId}
            onChange={(e) => setPatientId(e.target.value)}
            style={{
              padding: "8px 12px",
              borderRadius: "6px",
              border: "1px solid var(--chakra-colors-border)",
              width: "100%",
            }}
          >
            <option value="">Select patient</option>
            {patients.map((p) => (
              <option key={p.id} value={p.id}>
                {p.firstName} {p.lastName} (ID: {p.id})
              </option>
            ))}
          </select>
        </Field.Root>
        <Button variant="solid" size="sm" onClick={load} loading={loading}>
          Load record
        </Button>
        <Button variant="outline" size="sm" onClick={ensureOrCreate} loading={loading}>
          Get or create record
        </Button>
      </HStack>

      {error && (
        <Alert.Root status="error" mb={4}>
          <Alert.Indicator />
          <Alert.Title>Error</Alert.Title>
          <Alert.Description>{error}</Alert.Description>
        </Alert.Root>
      )}

      {loading && !record ? (
        <Spinner size="lg" />
      ) : record ? (
        <Stack gap={2} p={4} borderWidth="1px" borderRadius="md" bg="bg">
          <Text><strong>Record ID:</strong> {record.id}</Text>
          <Text><strong>Patient ID:</strong> {record.patientId}</Text>
          <Text><strong>Allergies:</strong> {record.allergies ?? "—"}</Text>
          <Text><strong>Current medications:</strong> {record.currentMedications ?? "—"}</Text>
          <Text><strong>Chronic conditions:</strong> {record.chronicConditions ?? "—"}</Text>
          <Text><strong>Family history:</strong> {record.familyHistory ?? "—"}</Text>
        </Stack>
      ) : (
        <Text color="fg.muted">
          Select a patient and click Load record, or Get or create record.
        </Text>
      )}
    </Box>
  );
}
