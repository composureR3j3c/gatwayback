import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.client.HttpClient;

public class NettyDnsConfig {

    public static HttpClient httpClientWithCustomDns() {
        return HttpClient.create()
                // Uses Netty async DNS (not JVM blocking resolver)
                .resolver(DefaultAddressResolverGroup.INSTANCE);
    }
}
