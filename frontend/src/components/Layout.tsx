import { Outlet, Link, useLocation } from "react-router-dom";
import { Box, HStack, Container, Button, Text } from "@chakra-ui/react";
import { useAuth } from "../providers/AuthProvider";

const navItems = [
  { path: "/dashboard", label: "Dashboard" },
  { path: "/patients", label: "Patients" },
  { path: "/staff", label: "Staff" },
  { path: "/appointments", label: "Appointments" },
  { path: "/medical-records", label: "Medical records" },
  { path: "/consultations", label: "Consultations" },
];

export function Layout() {
  const { username, logout } = useAuth();
  const location = useLocation();

  return (
    <Box minH="100vh" bg="bg.subtle">
      <Box borderBottomWidth="1px" bg="bg" py={2}>
        <Container maxW="6xl">
          <HStack justify="space-between" flexWrap="wrap" gap={2}>
            <HStack gap={6} flexWrap="wrap">
              {navItems.map((item) => (
                <Button
                  key={item.path}
                  variant={location.pathname === item.path || location.pathname.startsWith(item.path + "/") ? "solid" : "ghost"}
                  size="sm"
                  asChild
                >
                  <Link to={item.path}>{item.label}</Link>
                </Button>
              ))}
            </HStack>
            <HStack gap={2}>
              <Text fontSize="sm" color="fg.muted">
                {username}
              </Text>
              <Button variant="outline" size="sm" onClick={logout}>
                Logout
              </Button>
            </HStack>
          </HStack>
        </Container>
      </Box>
      <Container maxW="6xl" py={6}>
        <Outlet />
      </Container>
    </Box>
  );
}
