package net.godlycow.org.essc.test;

import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.EssentialsCAPI;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiProviderTest {

    @Test
    void providerRegistersAndServesApi() {
        assertFalse(APIProvider.isAvailable());
        assertThrows(IllegalStateException.class, APIProvider::get);
        assertThrows(IllegalArgumentException.class, () -> APIProvider.register(null));

        EssentialsCAPI stub = (EssentialsCAPI) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{EssentialsCAPI.class},
                (proxy, method, args) -> null);

        APIProvider.register(stub);
        try
        {
            assertTrue(APIProvider.isAvailable());
            assertSame(stub, APIProvider.get());
            assertThrows(IllegalStateException.class, () -> APIProvider.register(stub));
        } finally {
            APIProvider.unregister();
        }
        assertFalse(APIProvider.isAvailable());
    }
}
