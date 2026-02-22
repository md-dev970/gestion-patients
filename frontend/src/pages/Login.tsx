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
import { login } from "../api/auth";

export function Login() {
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const { data } = await login({ username, password });
      setAuth(data);
      navigate("/dashboard", { replace: true });
    } catch (err: unknown) {
      const msg =
        err && typeof err === "object" && "response" in err
          ? (err as { response?: { data?: { message?: string } } }).response
              ?.data?.message
          : "Login failed";
      setError(String(msg || "Login failed"));
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
          Sign in
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
                autoComplete="username"
              />
              {error && <Field.ErrorText>{error}</Field.ErrorText>}
            </Field.Root>
            <Field.Root>
              <Field.Label>Password</Field.Label>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
                required
                autoComplete="current-password"
              />
            </Field.Root>
            <Button type="submit" variant="solid" loading={loading}>
              Sign in
            </Button>
          </Stack>
        </form>
        <Text mt={4} fontSize="sm" color="fg.muted">
          No account?{" "}
          <Link to="/register" style={{ textDecoration: "underline" }}>
            Register
          </Link>
        </Text>
      </Box>
    </Container>
  );
}
