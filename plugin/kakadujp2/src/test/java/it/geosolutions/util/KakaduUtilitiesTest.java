package it.geosolutions.util;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import jdk.nashorn.internal.ir.annotations.Ignore;
import kdu_jni.Jp2_channels;

@RunWith(PowerMockRunner.class)
//delegate to standard runner so assumeTrue works
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
//suppress System.loadLibrary in static initialization of these classes
@SuppressStaticInitializationFor({ "kdu_jni.Kdu_global", "kdu_jni.Jp2_channels" })
public class KakaduUtilitiesTest {

    private static int JNI_VERSION;

    public static @BeforeClass void beforeClass() throws Exception {
        JNI_VERSION = KakaduUtilities.getKakaduJniMajorVersion();
    }

    private static class SetColorMappingCapturer implements Answer<Object> {

        List<List<Object>> invocationArguments = new ArrayList<>();

        public @Override Object answer(InvocationOnMock invocation) {
            Method method = invocation.getMethod();
            if ("Set_colour_mapping".equals(method.getName())) {
                Object[] arguments = invocation.getArguments();
                invocationArguments.add(asList(arguments));
            }
            return null;
        }
    };

    private SetColorMappingCapturer colorMappingCapturer;
    private Jp2_channels channels;

    public @Before void before() {
        colorMappingCapturer = new SetColorMappingCapturer();
        channels = PowerMockito.mock(Jp2_channels.class, colorMappingCapturer);
    }

    public @Test void testInitializeRGBChannels_KduPreV7() throws Exception {
        assumeTrue(format("JNI version=%d, expected < 7, ignoring", JNI_VERSION), JNI_VERSION < 7);

        KakaduUtilities.initializeRGBChannels(channels);

        verify(channels).Init(eq(3));
        List<List<Object>> args = colorMappingCapturer.invocationArguments;
        assertEquals(3, args.size());

        List<List<Object>> expectedCallsAndArgs = asList(asList(0, 0, 0), asList(1, 0, 1), asList(2, 0, 2));
        assertEquals(expectedCallsAndArgs, args);
    }

    public @Ignore @Test void testInitializeRGBChannels_KduV7Plus() throws Exception {
        assumeTrue(format("JNI version=%d, expected >= 7, ignoring", JNI_VERSION), JNI_VERSION >= 7);

        KakaduUtilities.initializeRGBChannels(channels);

        verify(channels).Init(eq(3));
        List<List<Object>> args = colorMappingCapturer.invocationArguments;
        assertEquals(3, args.size());

        List<List<Object>> expectedCallsAndArgs = asList(asList(0, 0, 0, 0, 0), asList(1, 0, 1, 0, 0),
                asList(2, 0, 2, 0, 0));
        assertEquals(expectedCallsAndArgs, args);
    }
}
