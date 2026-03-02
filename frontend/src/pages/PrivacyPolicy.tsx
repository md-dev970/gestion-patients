import { Link } from "react-router-dom";
import {
  Box,
  Container,
  Heading,
  Text,
  Stack,
  Button,
} from "@chakra-ui/react";

export function PrivacyPolicy() {
  return (
    <Container maxW="3xl" py={12}>
      <Box
        p={8}
        borderRadius="lg"
        borderWidth="1px"
        bg="bg"
        shadow="sm"
      >
        <Heading size="xl" mb={6}>
          Privacy Policy
        </Heading>
        <Stack gap={4}>
          <Text>
            Last updated: {new Date().toLocaleDateString()}
          </Text>
          <Heading size="md">1. Introduction</Heading>
          <Text>
            The Hospital Management System (KIT COMMUN) is committed to protecting your privacy. This Privacy Policy explains how we collect, use, store, and protect your personal and health information in compliance with the General Data Protection Regulation (RGPD) and applicable healthcare regulations.
          </Text>
          <Heading size="md">2. Information We Collect</Heading>
          <Text>
            We collect information you provide when registering (username, email, role) and when using the platform (patient data, medical records, consultations, appointments). We also collect technical data such as IP address and usage logs for security and audit purposes.
          </Text>
          <Heading size="md">3. Legal Basis for Processing</Heading>
          <Text>
            We process your data based on your consent, the performance of a contract, or our legitimate interests in providing healthcare services. You may withdraw consent at any time for processing based on consent.
          </Text>
          <Heading size="md">4. How We Use Your Information</Heading>
          <Text>
            Your information is used to provide healthcare services, manage appointments and medical records, ensure security (authentication, anti-bruteforce, audit logs), and comply with legal obligations.
          </Text>
          <Heading size="md">5. Data Retention</Heading>
          <Text>
            We retain your data according to our retention policy (typically 10 years for medical records as required by law). After the retention period, data is securely purged. Audit logs are retained for 90 days to 1 year depending on the event type.
          </Text>
          <Heading size="md">6. Your Rights</Heading>
          <Text>
            You have the right to access, rectify, erase, restrict processing, and port your data. You may also withdraw consent and lodge a complaint with a supervisory authority. Contact the Data Protection Officer (DPO) for any requests.
          </Text>
          <Heading size="md">7. Security</Heading>
          <Text>
            We implement appropriate technical and organizational measures to protect your data, including encryption in transit (HTTPS), secure authentication, and access controls.

          </Text>
        </Stack>
        <Box mt={8}>
          <Button asChild variant="outline" size="sm">
            <Link to="/register">Back to Register</Link>
          </Button>
        </Box>
      </Box>
    </Container>
  );
}
