import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Box,
  Button,
  Container,
  Field,
  Heading,
  Input,
  Stack,
  Text,
} from "@chakra-ui/react";
import { useAuth } from "../providers/AuthProvider";
import { register as registerApi } from "../api/auth";

export function Register() {
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("ROLE_PATIENT");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const roleOptions = [
    { value: "ROLE_PATIENT", label: "Patient" },
    { value: "ROLE_DOCTOR", label: "Doctor" },
    { value: "ROLE_MEDECIN", label: "Médecin" },
    { value: "ROLE_NURSE", label: "Nurse" },
    { value: "ROLE_INFIRMIER", label: "Infirmier" },
    { value: "ROLE_RECEPTIONIST", label: "Receptionist" },
    { value: "ROLE_LAB_TECH", label: "Lab technician" },
    { value: "ROLE_ADMIN", label: "Admin" },
  ] as const;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const { data } = await registerApi({ username, email, password, role });
      setAuth(data);
      navigate("/dashboard", { replace: true });
    } catch (err: unknown) {
      const msg =
        err && typeof err === "object" && "response" in err
          ? (err as { response?: { data?: { message?: string } } }).response
              ?.data?.message
          : "Registration failed";
      setError(String(msg || "Registration failed"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <Container maxW="md" py={12}>
      <Box
        p={6}
        borderRadius="lg"
        borderWidth="1px"
        bg="bg"
        shadow="sm"
      >
        <Heading size="lg" mb={4}>
          Register
        </Heading>
        <form onSubmit={handleSubmit}>
          <Stack gap={4}>
            <Field.Root invalid={!!error}>
              <Field.Label>Username</Field.Label>
              <Input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Username"
                required
                minLength={3}
                maxLength={50}
                autoComplete="username"
              />
              {error && <Field.ErrorText>{error}</Field.ErrorText>}
            </Field.Root>
            <Field.Root>
              <Field.Label>Email</Field.Label>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email"
                required
                autoComplete="email"
              />
            </Field.Root>
            <Field.Root>
              <Field.Label>Password</Field.Label>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password (min 8 characters)"
                required
                minLength={8}
                autoComplete="new-password"
              />
            </Field.Root>
            <Field.Root>
              <Field.Label>Role</Field.Label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                style={{
                  width: "100%",
                  padding: "8px 12px",
                  borderRadius: "var(--chakra-radii-md)",
                  border: "1px solid var(--chakra-colors-border)",
                  fontSize: "inherit",
                }}
              >
                {roleOptions.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
              <Field.HelperText>
                Staff roles can access patients, consultations, etc. Patient is default.
              </Field.HelperText>
            </Field.Root>
            <Button type="submit" variant="solid" loading={loading}>
              Register
            </Button>
          </Stack>
        </form>
        <Text mt={4} fontSize="sm" color="fg.muted">
          Already have an account?{" "}
          <Link to="/login" style={{ textDecoration: "underline" }}>
            Sign in
          </Link>
        </Text>
      </Box>
    </Container>
  );
}
