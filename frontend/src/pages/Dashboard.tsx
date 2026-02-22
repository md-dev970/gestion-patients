import { Link } from "react-router-dom";
import { Box, Heading, Stack, Text } from "@chakra-ui/react";
import { Button } from "@chakra-ui/react";

const links = [
  { to: "/patients", label: "Patients" },
  { to: "/staff", label: "Staff" },
  { to: "/appointments", label: "Appointments" },
  { to: "/medical-records", label: "Medical records" },
  { to: "/consultations", label: "Consultations" },
];

export function Dashboard() {
  return (
    <Box>
      <Heading size="lg" mb={4}>
        Dashboard
      </Heading>
      <Text color="fg.muted" mb={6}>
        KIT COMMUN — Select a section below.
      </Text>
      <Stack direction="row" flexWrap="wrap" gap={3}>
        {links.map(({ to, label }) => (
          <Button key={to} asChild variant="outline" size="md">
            <Link to={to}>{label}</Link>
          </Button>
        ))}
      </Stack>
    </Box>
  );
}
