import { useEffect, useState } from "react";
import {
  Box,
  Heading,
  Table,
  Text,
  Spinner,
  Alert,
} from "@chakra-ui/react";
import { fetchConsultations, type Consultation } from "../api/consultations";

export function Consultations() {
  const [items, setItems] = useState<Consultation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    fetchConsultations()
      .then((res) => {
        if (!cancelled) setItems(res.data ?? []);
      })
      .catch((err) => {
        if (!cancelled)
          setError(
            err.response?.data?.message ?? "Failed to load consultations"
          );
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  if (loading) {
    return (
      <Box py={8}>
        <Spinner size="lg" />
      </Box>
    );
  }

  if (error) {
    return (
      <Box py={4}>
        <Alert.Root status="error">
          <Alert.Indicator />
          <Alert.Title>Error</Alert.Title>
          <Alert.Description>{error}</Alert.Description>
        </Alert.Root>
      </Box>
    );
  }

  return (
    <Box>
      <Heading size="lg" mb={4}>
        Consultations
      </Heading>
      <Table.Root size="sm">
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>ID</Table.ColumnHeader>
            <Table.ColumnHeader>Patient</Table.ColumnHeader>
            <Table.ColumnHeader>Doctor (user)</Table.ColumnHeader>
            <Table.ColumnHeader>Date</Table.ColumnHeader>
            <Table.ColumnHeader>Type</Table.ColumnHeader>
            <Table.ColumnHeader>Status</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {items.length === 0 ? (
            <Table.Row>
              <Table.Cell colSpan={6}>
                <Text color="fg.muted">No consultations found.</Text>
              </Table.Cell>
            </Table.Row>
          ) : (
            items.map((c) => (
              <Table.Row key={c.consultationId}>
                <Table.Cell>{c.consultationId}</Table.Cell>
                <Table.Cell>{c.patientId}</Table.Cell>
                <Table.Cell>{c.userId}</Table.Cell>
                <Table.Cell>
                  {c.consultationDate
                    ? new Date(c.consultationDate).toLocaleString()
                    : "—"}
                </Table.Cell>
                <Table.Cell>{c.consultationType ?? "—"}</Table.Cell>
                <Table.Cell>{c.status ?? "—"}</Table.Cell>
              </Table.Row>
            ))
          )}
        </Table.Body>
      </Table.Root>
    </Box>
  );
}
