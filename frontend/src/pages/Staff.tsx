import { useEffect, useState } from "react";
import {
  Box,
  Heading,
  Table,
  Text,
  Spinner,
  Alert,
} from "@chakra-ui/react";
import { fetchStaff, type Staff as StaffType } from "../api/staff";

export function Staff() {
  const [items, setItems] = useState<StaffType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    fetchStaff()
      .then((res) => {
        if (!cancelled) setItems(res.data ?? []);
      })
      .catch((err) => {
        if (!cancelled)
          setError(err.response?.data?.message ?? "Failed to load staff");
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
        Staff
      </Heading>
      <Table.Root size="sm">
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>ID</Table.ColumnHeader>
            <Table.ColumnHeader>First name</Table.ColumnHeader>
            <Table.ColumnHeader>Last name</Table.ColumnHeader>
            <Table.ColumnHeader>Email</Table.ColumnHeader>
            <Table.ColumnHeader>Role</Table.ColumnHeader>
            <Table.ColumnHeader>Specialty</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {items.length === 0 ? (
            <Table.Row>
              <Table.Cell colSpan={6}>
                <Text color="fg.muted">No staff found.</Text>
              </Table.Cell>
            </Table.Row>
          ) : (
            items.map((s) => (
              <Table.Row key={s.id}>
                <Table.Cell>{s.id}</Table.Cell>
                <Table.Cell>{s.firstName}</Table.Cell>
                <Table.Cell>{s.lastName}</Table.Cell>
                <Table.Cell>{s.email ?? "—"}</Table.Cell>
                <Table.Cell>{s.role ?? "—"}</Table.Cell>
                <Table.Cell>{s.specialty ?? "—"}</Table.Cell>
              </Table.Row>
            ))
          )}
        </Table.Body>
      </Table.Root>
    </Box>
  );
}
