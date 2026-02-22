import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  Box,
  Button,
  Field,
  Heading,
  Input,
  Stack,
  HStack,
} from "@chakra-ui/react";
import { createPatient, type PatientCreate } from "../api/patients";

export function PatientNew() {
  const navigate = useNavigate();
  const [form, setForm] = useState<PatientCreate>({
    firstName: "",
    lastName: "",
    nationalId: "",
    dateOfBirth: "",
    email: "",
    phoneNumber: "",
    address: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function update(key: keyof PatientCreate, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const payload = { ...form };
      if (!payload.nationalId) delete payload.nationalId;
      if (!payload.dateOfBirth) delete payload.dateOfBirth;
      if (!payload.email) delete payload.email;
      if (!payload.phoneNumber) delete payload.phoneNumber;
      if (!payload.address) delete payload.address;
      await createPatient(payload);
      navigate("/patients", { replace: true });
    } catch (err: unknown) {
      const msg =
        err && typeof err === "object" && "response" in err
          ? (err as { response?: { data?: { message?: string } } }).response
              ?.data?.message
          : "Failed to create patient";
      setError(String(msg || "Failed to create patient"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <Box>
      <Heading size="lg" mb={4}>
        New patient
      </Heading>
      <form onSubmit={handleSubmit}>
        <Stack gap={4} maxW="md">
          <Field.Root invalid={!!error}>
            <Field.Label>First name</Field.Label>
            <Input
              value={form.firstName}
              onChange={(e) => update("firstName", e.target.value)}
              required
            />
            {error && <Field.ErrorText>{error}</Field.ErrorText>}
          </Field.Root>
          <Field.Root>
            <Field.Label>Last name</Field.Label>
            <Input
              value={form.lastName}
              onChange={(e) => update("lastName", e.target.value)}
              required
            />
          </Field.Root>
          <Field.Root>
            <Field.Label>National ID</Field.Label>
            <Input
              value={form.nationalId ?? ""}
              onChange={(e) => update("nationalId", e.target.value)}
            />
          </Field.Root>
          <Field.Root>
            <Field.Label>Date of birth</Field.Label>
            <Input
              type="date"
              value={form.dateOfBirth ?? ""}
              onChange={(e) => update("dateOfBirth", e.target.value)}
            />
          </Field.Root>
          <Field.Root>
            <Field.Label>Email</Field.Label>
            <Input
              type="email"
              value={form.email ?? ""}
              onChange={(e) => update("email", e.target.value)}
            />
          </Field.Root>
          <Field.Root>
            <Field.Label>Phone</Field.Label>
            <Input
              value={form.phoneNumber ?? ""}
              onChange={(e) => update("phoneNumber", e.target.value)}
            />
          </Field.Root>
          <Field.Root>
            <Field.Label>Address</Field.Label>
            <Input
              value={form.address ?? ""}
              onChange={(e) => update("address", e.target.value)}
            />
          </Field.Root>
          <HStack gap={2}>
            <Button type="submit" variant="solid" loading={loading}>
              Create
            </Button>
            <Button asChild variant="outline">
              <Link to="/patients">Cancel</Link>
            </Button>
          </HStack>
        </Stack>
      </form>
    </Box>
  );
}
