import { useState } from "react";
import {
  Box,
  Button,
  Field,
  Heading,
  Input,
  Table,
  Text,
  Spinner,
  Alert,
  HStack,
} from "@chakra-ui/react";
import {
  fetchAppointmentsByPatient,
  fetchAppointmentsByDoctor,
  type Appointment,
} from "../api/appointments";

type Mode = "patient" | "doctor";

export function Appointments() {
  const [mode, setMode] = useState<Mode>("patient");
  const [id, setId] = useState("");
  const [items, setItems] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function load() {
    const num = Number(id);
    if (!id || Number.isNaN(num)) {
      setError("Enter a valid ID");
      return;
    }
    setError("");
    setLoading(true);
    const request =
      mode === "patient"
        ? fetchAppointmentsByPatient(num)
        : fetchAppointmentsByDoctor(num);
    request
      .then((res) => setItems(res.data ?? []))
      .catch((err) => {
        setError(err.response?.data?.message ?? "Failed to load appointments");
        setItems([]);
      })
      .finally(() => setLoading(false));
  }

  return (
    <Box>
      <Heading size="lg" mb={4}>
        Appointments
      </Heading>
      <HStack gap={4} mb={4} flexWrap="wrap">
        <HStack gap={2}>
          <Button
            variant={mode === "patient" ? "solid" : "outline"}
            size="sm"
            onClick={() => setMode("patient")}
          >
            By patient
          </Button>
          <Button
            variant={mode === "doctor" ? "solid" : "outline"}
            size="sm"
            onClick={() => setMode("doctor")}
          >
            By doctor
          </Button>
        </HStack>
        <Field.Root style={{ maxWidth: 120 }}>
          <Field.Label>{mode === "patient" ? "Patient ID" : "Doctor ID"}</Field.Label>
          <Input
            type="number"
            value={id}
            onChange={(e) => setId(e.target.value)}
            placeholder={mode === "patient" ? "Patient ID" : "Doctor ID"}
          />
        </Field.Root>
        <Button variant="solid" size="sm" onClick={load} loading={loading}>
          Load
        </Button>
      </HStack>

      {error && (
        <Alert.Root status="error" mb={4}>
          <Alert.Indicator />
          <Alert.Title>Error</Alert.Title>
          <Alert.Description>{error}</Alert.Description>
        </Alert.Root>
      )}

      {loading && items.length === 0 ? (
        <Spinner size="lg" />
      ) : (
        <Table.Root size="sm">
          <Table.Header>
            <Table.Row>
              <Table.ColumnHeader>ID</Table.ColumnHeader>
              <Table.ColumnHeader>Patient</Table.ColumnHeader>
              <Table.ColumnHeader>Doctor</Table.ColumnHeader>
              <Table.ColumnHeader>Date / time</Table.ColumnHeader>
              <Table.ColumnHeader>Status</Table.ColumnHeader>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {items.length === 0 ? (
              <Table.Row>
                <Table.Cell colSpan={5}>
                  <Text color="fg.muted">
                    Select an ID and click Load to view appointments.
                  </Text>
                </Table.Cell>
              </Table.Row>
            ) : (
              items.map((a) => (
                <Table.Row key={a.id}>
                  <Table.Cell>{a.id}</Table.Cell>
                  <Table.Cell>{a.patientId}</Table.Cell>
                  <Table.Cell>{a.doctorId}</Table.Cell>
                  <Table.Cell>
                    {a.appointmentDateTime
                      ? new Date(a.appointmentDateTime).toLocaleString()
                      : "—"}
                  </Table.Cell>
                  <Table.Cell>{a.status ?? "—"}</Table.Cell>
                </Table.Row>
              ))
            )}
          </Table.Body>
        </Table.Root>
      )}
    </Box>
  );
}
