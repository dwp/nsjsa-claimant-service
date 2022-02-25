package uk.gov.dwp.jsa.claimant.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.jsa.security.KeyLoader;

import java.security.PrivateKey;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrivateKeyProviderTest {

    private static final String VALID_KEY = "validKey";


    @Mock
    private KeyLoader<String> mockKeyLoader;

    @Mock
    private PrivateKey mockPrivateKey;

    @Before
    public void setUp() {
        when(mockKeyLoader.loadPrivateKey(VALID_KEY)).thenReturn(mockPrivateKey);
    }

    @Test
    public void test_PrivateKeyLoadedWhenAgentModeOff() {
        PrivateKeyProvider testSubject = new PrivateKeyProvider(VALID_KEY, mockKeyLoader);
        assertEquals(mockPrivateKey, testSubject.getPrivateKey());
    }

    @Test
    public void test_PrivateKeyNullWhenAgentModeOffAndPrivateStringEmpty() {
        PrivateKeyProvider testSubject = new PrivateKeyProvider(EMPTY, mockKeyLoader);
        assertNull(testSubject.getPrivateKey());
    }

    @Test
    public void test_PrivateKeyNullWhenAgentModeOffAndPrivateStringNull() {
        PrivateKeyProvider testSubject = new PrivateKeyProvider(null, mockKeyLoader);
        assertNull(testSubject.getPrivateKey());
    }

}
