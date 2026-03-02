import { Link } from "react-router-dom";
import {
  Box,
  Container,
  Heading,
  Text,
  Stack,
  Button,
} from "@chakra-ui/react";

export function TermsAndConditions() {
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
          Terms and Conditions
        </Heading>
        <Stack gap={4}>
          <Text>
            Last updated: {new Date().toLocaleDateString()}
          </Text>
          <Heading size="md">1. Acceptance of Terms</Heading>
          <Text>
            By accessing and using the Hospital Management System (KIT COMMUN), you accept and agree to be bound by these Terms and Conditions. If you do not agree to these terms, please do not use this service.
          </Text>
          <Heading size="md">2. Use of Service</Heading>
          <Text>
            This platform is intended for authorized healthcare personnel and patients. You agree to use the service only for lawful purposes and in accordance with applicable healthcare regulations and data protection laws (RGPD, HIPAA).
          </Text>
          <Heading size="md">3. User Accounts</Heading>
          <Text>
            You are responsible for maintaining the confidentiality of your account credentials. You must notify us immediately of any unauthorized use of your account.
          </Text>
          <Heading size="md">4. Data Protection</Heading>
          <Text>
            Your use of this service is also governed by our Privacy Policy. By using this service, you consent to the collection and use of your information as described in the Privacy Policy.
          </Text>
          <Heading size="md">5. Limitation of Liability</Heading>
          <Text>
            The service is provided "as is". We do not warrant that the service will be uninterrupted or error-free. In no event shall we be liable for any indirect, incidental, or consequential damages arising from your use of the service.
          </Text>
          <Heading size="md">6. Changes</Heading>
          <Text>
            We reserve the right to modify these terms at any time. Continued use of the service after changes constitutes acceptance of the modified terms.
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
