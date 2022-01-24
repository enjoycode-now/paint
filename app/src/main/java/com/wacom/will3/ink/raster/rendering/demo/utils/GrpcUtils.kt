package com.wacom.will3.ink.raster.rendering.demo.utils

import android.annotation.SuppressLint
import android.graphics.*
import com.google.protobuf.StringValue
import com.wacom.will3.ink.raster.rendering.demo.PaintServiceGrpc
import com.wacom.will3.ink.raster.rendering.demo.RoomServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import java.util.concurrent.TimeUnit

lateinit var drawStub: PaintServiceGrpc.PaintServiceStub
lateinit var drawBlockingStub: PaintServiceGrpc.PaintServiceBlockingStub
lateinit var roomStub: RoomServiceGrpc.RoomServiceStub
lateinit var roomBlockingStub: RoomServiceGrpc.RoomServiceBlockingStub

val env = "dev"
val defaultServer="${env}.unicorn.org.cn"
val defaultPort=18010

@SuppressLint("StaticFieldLeak")
lateinit var drawChannel:ManagedChannel
lateinit var roomChannel:ManagedChannel

fun buildLoginStub(){

    roomChannel = ManagedChannelBuilder
        .forAddress(defaultServer,defaultPort)
        .usePlaintext()
        .enableRetry()
        .maxRetryAttempts(10)
        .keepAliveTime(5,TimeUnit.SECONDS)
        .keepAliveTimeout(5,TimeUnit.SECONDS)
        .build()

    drawChannel = ManagedChannelBuilder
        .forAddress(defaultServer,defaultPort)
        .usePlaintext()
        .enableRetry()
        .maxRetryAttempts(10)
        .keepAliveTime(5,TimeUnit.SECONDS)
        .keepAliveTimeout(5,TimeUnit.SECONDS)
        .build()

    drawStub = PaintServiceGrpc.newStub(drawChannel)
    drawBlockingStub = PaintServiceGrpc.newBlockingStub(drawChannel)

    roomStub = RoomServiceGrpc.newStub(roomChannel)
    roomBlockingStub= RoomServiceGrpc.newBlockingStub(roomChannel)
}

fun shutDownChannel(){
    if(!drawChannel.isShutdown) drawChannel.shutdown()
    if(!roomChannel.isShutdown) roomChannel.shutdown()
}

fun setGrpcToken(token:StringValue){
    // 发送请求时加上 Token
    val HEADER_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
    val headers = Metadata()
    headers.put(HEADER_KEY, "Bearer ${token.value}")
    drawStub = MetadataUtils.attachHeaders(drawStub,headers)
    drawBlockingStub = MetadataUtils.attachHeaders(drawBlockingStub,headers)
    roomStub = MetadataUtils.attachHeaders(roomStub,headers)
    roomBlockingStub= MetadataUtils.attachHeaders(roomBlockingStub,headers)
}

