import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Box,
  Button,
  Heading,
  Table,
  Text,
  Spinner,
  Alert,
  HStack,
} from "@chakra-ui/react";
import { fetchPatients, type Patient } from "../api/patients";

export function Patients() {
  const [items, setItems] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    fetchPatients()
      .then((res) => {
        if (!cancelled) setItems(res.data ?? []);
      })
      .catch((err) => {
        if (!cancelled)
          setError(err.response?.data?.message ?? "Failed to load patients");
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
      <HStack justify="space-between" mb={4}>
        <Heading size="lg">Patients</Heading>
        <Button asChild variant="solid" size="sm">
          <Link to="/patients/new">New patient</Link>
        </Button>
      </HStack>
      <Table.Root size="sm">
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>ID</Table.ColumnHeader>
            <Table.ColumnHeader>First name</Table.ColumnHeader>
            <Table.ColumnHeader>Last name</Table.ColumnHeader>
            <Table.ColumnHeader>Email</Table.ColumnHeader>
            <Table.ColumnHeader>Phone</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {items.length === 0 ? (
            <Table.Row>
              <Table.Cell colSpan={5}>
                <Text color="fg.muted">No patients found.</Text>
              </Table.Cell>
            </Table.Row>
          ) : (
            items.map((p) => (
              <Table.Row key={p.id}>
                <Table.Cell>{p.id}</Table.Cell>
                <Table.Cell>{p.firstName}</Table.Cell>
                <Table.Cell>{p.lastName}</Table.Cell>
                <Table.Cell>{p.email ?? "—"}</Table.Cell>
                <Table.Cell>{p.phoneNumber ?? "—"}</Table.Cell>
              </Table.Row>
            ))
          )}
        </Table.Body>
      </Table.Root>
    </Box>
  );
}
