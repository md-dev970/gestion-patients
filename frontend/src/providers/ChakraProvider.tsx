import {
  ChakraProvider as ChakraProviderRoot,
  createSystem,
  defaultConfig,
} from "@chakra-ui/react";

const system = createSystem(defaultConfig, {
  theme: {},
});

export function ChakraProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ChakraProviderRoot value={system}>{children}</ChakraProviderRoot>
  );
}
