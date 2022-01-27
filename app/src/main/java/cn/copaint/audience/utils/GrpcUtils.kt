package cn.copaint.audience.utils

import com.google.protobuf.StringValue
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import paint.v1.PaintingServiceGrpc
import paint.v1.PaintingServiceGrpc.PaintingServiceBlockingStub
import java.util.concurrent.TimeUnit

object GrpcUtils {
    val grpcServer="dev.unicorn.org.cn"
    val grpcPort=8880

    lateinit var paintChannel: ManagedChannel
    lateinit var paintStub:PaintingServiceBlockingStub

    fun buildStub(){
        paintChannel = ManagedChannelBuilder
            .forAddress(grpcServer,grpcPort)
            .usePlaintext()
            .enableRetry()
            .maxRetryAttempts(3)
            .idleTimeout(5, TimeUnit.SECONDS)
            .keepAliveTime(5, TimeUnit.SECONDS)
            .keepAliveTimeout(1, TimeUnit.SECONDS)
            .build()
        paintStub = PaintingServiceGrpc.newBlockingStub(paintChannel)
    }

    fun setToken(token: StringValue){
        // 发送请求时加上 Token
        val HEADER_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
        val headers = Metadata()
        headers.put(HEADER_KEY, "Bearer "+token.value)
        paintStub = MetadataUtils.attachHeaders(paintStub,headers)
    }

    fun shutDownChannel(){
        if(!paintChannel.isShutdown) paintChannel.shutdown()
    }
}