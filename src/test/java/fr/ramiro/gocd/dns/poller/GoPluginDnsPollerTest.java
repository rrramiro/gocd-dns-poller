package fr.ramiro.gocd.dns.poller;

import org.junit.Test;
import static org.junit.Assert.*;

public class GoPluginDnsPollerTest {

    @Test
    public void testInit(){
        GoPluginDnsPoller plugin = new GoPluginDnsPoller();
        assertNotNull(plugin);
    }
}
