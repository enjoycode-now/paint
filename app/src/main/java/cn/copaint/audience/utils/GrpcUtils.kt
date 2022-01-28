package cn.copaint.audience.utils

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import paint.v1.PaintingServiceGrpcKt
import java.util.concurrent.TimeUnit

object GrpcUtils {
    val grpcServer = "120.78.173.15"
    val grpcPort = 19999

    lateinit var paintChannel: ManagedChannel
    lateinit var paintStub: PaintingServiceGrpcKt.PaintingServiceCoroutineStub

    fun buildStub() {
        paintChannel = ManagedChannelBuilder
            .forAddress(grpcServer, grpcPort)
            .usePlaintext()
            .enableRetry()
            .maxRetryAttempts(3)
            .idleTimeout(5, TimeUnit.SECONDS)
            .keepAliveTime(5, TimeUnit.SECONDS)
            .keepAliveTimeout(1, TimeUnit.SECONDS)
            .build()
        paintStub = PaintingServiceGrpcKt.PaintingServiceCoroutineStub(paintChannel)
    }

    fun setToken(token: String) {
        // 发送请求时加上 Token
        val HEADER_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
        val headers = Metadata()
        headers.put(HEADER_KEY, "Bearer $token")
        paintStub = MetadataUtils.attachHeaders(paintStub, headers)
    }

    fun setPaintId(id: String) {
        // 发送请求时加上 Token
        val HEADER_KEY = Metadata.Key.of("x-painting-id", Metadata.ASCII_STRING_MARSHALLER)
        val headers = Metadata()
        headers.put(HEADER_KEY, id)
        paintStub = MetadataUtils.attachHeaders(paintStub, headers)
    }

    fun shutDownChannel() {
        if (!paintChannel.isShutdown) paintChannel.shutdown()
    }
}
